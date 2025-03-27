package org.nocturne.sockets

import okhttp3.internal.closeQuietly
import okio.ByteString.Companion.encodeUtf8
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.util.concurrent.ConcurrentLinkedQueue

/**
 * TODO - Add a command queue to send/execute commands.
 * TODO - Heartbeat will send every X seconds to ensure client is connected.
 * TODO - Add a timeout.
 */
class ClientWorkerConnection(val socket: Socket) {
    val logger = LoggerFactory.getLogger(ClientWorkerConnection::class.java)
    val HEARTBEAT_COOLDOWN = 20000L
    lateinit var writer: PrintWriter    // Autoflush is off, please clean up after yourself =)
    lateinit var reader: BufferedReader
    var isAuthenticated = false
    @Volatile
    var isRunning = true

    val writeLock = Object()
    val cmdQueue = ConcurrentLinkedQueue<Pair<WorkerCommand, CommandResultLock>>()
    var queueWorker: Thread? = null
    var heartbeatWorker: Thread? = null

    fun start() {
        writer = PrintWriter(socket.getOutputStream(), false)
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        queueWorker = startQueueHandler()
        requestAuth().waitBlocking(5000)
        heartbeatWorker = startHeartbeatHandler()
    }

    fun close() {
        logger.info("Closing Socket...")
        try {
            writer.close()
            reader.close()
            socket.close()
        } catch (e: Exception) {
            logger.info("Exception closing socket ${e.stackTraceToString()}")
        }
        queueWorker?.interrupt()
        heartbeatWorker?.interrupt()
    }

    fun requestAuth(): CommandResultLock {
        return queueCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_AUTH).build())
    }

    fun requestEcho(param: String): CommandResultLock {
        return queueCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_ECHO)
            .addParam(param).build())
    }

    fun requestToxicCheck(param: String): CommandResultLock {
        return queueCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_TOXIC_CHECK)
            .addParam(param).build())
    }

    private fun raw_requestAuth(): Boolean {
        val requestDataLen = "@reply[123456]".length + SocketManager.socketAuth.length + "[123456]".length + 1

        writeCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_AUTH)
            .build())
        val result = getReplyDataBounded(requestDataLen)
        if (result != SocketManager.socketAuth) {
            logger.warn("INVALID AUTHENTICATION RECEIVED FROM ${socket.inetAddress}!")
            return false
        } else {
            logger.info("Authenticated Client")
            isAuthenticated = true
            return true
        }
    }

    fun raw_requestEcho(echoData: String?): String {
        if (!raw_ensureAuth()) { throw IOException("Socket Unauthorized!") }
        if (echoData == null) return ""
        writeCommand(
            WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_ECHO)
                .addParam(echoData)
                .build())
        val result = getReplyData()
        return result
    }

    fun raw_requestToxic(prompt: String?): String {
        if (!raw_ensureAuth()) { throw IOException("Socket Unauthorized!")}
        if (prompt == null) return ""
        writeCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_TOXIC_CHECK)
            .addParam(prompt)
            .build())
        val result = getReplyData()
        return result
    }

    /**
     * Add command into queue, return lock for sender to wait on.
     */
    private fun queueCommand(cmd: WorkerCommand): CommandResultLock {
        val lock = CommandResultLock()
        cmdQueue.add(Pair(cmd, lock))
        return lock
    }

    private fun startQueueHandler(): Thread {
        val t = Thread {
            while (isRunning) {
                try {
                    Thread.sleep(300)
                    val (cmd, resultLock) = cmdQueue.poll() ?: continue
                    val res = handleCommand(cmd)
                    resultLock.__complete(res)
                } catch (e: Exception) {
                    logger.error("Error running command")
                    logger.error(e.stackTraceToString())
                }
            }
        }
        t.start()
        return t
    }
    private fun startHeartbeatHandler(): Thread {
        val t = Thread {
            while (isRunning) {
                try {
                    val result = requestEcho("HEARTBEAT").waitBlocking(15000L)
                    if (result == null || result != "HEARTBEAT") throw IOException("Heartbeat failure.")
                    else {
                        logger.info("Socket Heartbeat Success")
                        Thread.sleep(HEARTBEAT_COOLDOWN)
                    }
                } catch(e: Exception) {
                    isRunning = false
                    logger.warn("Heartbeat failed! Shutting down! ${e.stackTraceToString()}")
                    this.close()
                    return@Thread
                }
            }
        }
        t.start()
        return t
    }

    private fun handleCommand(cmd: WorkerCommand): String {
        var result = ""
        when (cmd.cmd) {
            WorkerCommand.CMD_REQUEST_AUTH -> result = raw_requestAuth().toString()
            WorkerCommand.CMD_REQUEST_ECHO -> result = raw_requestEcho(cmd.params.firstOrNull())
            WorkerCommand.CMD_REQUEST_TOXIC_CHECK -> result = raw_requestToxic(cmd.params.firstOrNull())
            else -> {}
        }
        return result
    }

    /**
     * @see WorkerCommand
     * Will write a normal command with params
     */
    private fun writeCommand(cmd: WorkerCommand) {
        assertInit()
        val sendData = cmd.toTransmitString()
        synchronized(writeLock) {
            logger.debug("Sending Command ${sendData}")
            writer.println(sendData)
            writer.flush()
        }
    }

    // Use this so we dont read too much data when we shouldn't be!
    private fun getReplyDataBounded(exactChar: Int): String {
        val buf = CharArray(exactChar)
        reader.read(buf, 0, exactChar)
        var result = String(buf)
        if (!result.startsWith(WorkerCommand.REPLY_PREFIX)) {
            logger.warn("Received BAD reply data, discarding all!")
            clearReadBuffer()
            return ""
        }
        result = result.removePrefix(WorkerCommand.REPLY_PREFIX)
        result = result.removeSuffix("\n")
        while (!result.endsWith(WorkerCommand.REPLY_SUFFIX)) {
            result += reader.readLine()
        }
        if (result.endsWith(WorkerCommand.REPLY_SUFFIX)) {
            result = result.removeSuffix(WorkerCommand.REPLY_SUFFIX)
        }
        return result
    }

    private fun getReplyData(): String {
        var result = reader.readLine()
        if (result == null || !result.startsWith(WorkerCommand.REPLY_PREFIX)) {
            logger.warn("Received BAD reply data, discarding all!")
            clearReadBuffer()
            return ""
        }
        result = result.removePrefix(WorkerCommand.REPLY_PREFIX)
        while (!result.endsWith(WorkerCommand.REPLY_SUFFIX)) {
            result += reader.readLine()
        }
        if (result.endsWith(WorkerCommand.REPLY_SUFFIX)) {
            result = result.removeSuffix(WorkerCommand.REPLY_SUFFIX)
        }
        return result
    }

    /**
     * Try reading all data, then interrupt.
     * Might want to close socket and expect client to restart
     * if this is causing an issue.
     */
    private fun clearReadBuffer() {
        val clearThread = Thread {
            while (true) {
                reader.readLine()
            }
        }
        clearThread.start()
        Thread.sleep(3000)
        clearThread.interrupt()
    }

    private fun raw_ensureAuth(): Boolean {
        if (isAuthenticated) return true
        return raw_requestAuth()
    }

    /**
     * Ensures that writer/reader are initialized
     */
    private fun assertInit() {
        assert(this::writer.isInitialized)
        assert(this::reader.isInitialized)
    }
}