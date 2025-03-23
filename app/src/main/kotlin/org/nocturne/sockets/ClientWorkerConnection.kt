package org.nocturne.sockets

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
    lateinit var writer: PrintWriter    // Autoflush is off, please clean up after yourself =)
    lateinit var reader: BufferedReader
    var isAuthenticated = false
    var isRunning = true

    val writeLock = Object()
    val cmdQueue = ConcurrentLinkedQueue<Pair<WorkerCommand, CommandResultLock>>()
    var queueWorker: Thread? = null

    fun start() {
        writer = PrintWriter(socket.getOutputStream(), false)
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        queueWorker = startQueueHandler()
        requestAuth().waitBlocking(5000)
        requestEcho("Starting Server!").waitCallback(5000) { result ->
            if (result == null) { logger.warn("Socket Echo Failed") }
            else logger.info("Socket Echo: $result")
        }
    }

    fun requestAuth(): CommandResultLock {
        return queueCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_AUTH).build())
    }

    fun requestEcho(param: String): CommandResultLock {
        return queueCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_ECHO)
            .addParam(param).build())
    }


    private fun raw_requestAuth(): Boolean {
        writeCommand(WorkerCommand.Builder(WorkerCommand.CMD_REQUEST_AUTH)
            .build())
        val result = getReplyData()
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
                    Thread.sleep(1000)
                    var (cmd, resultLock) = cmdQueue.poll() ?: continue
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

    private fun handleCommand(cmd: WorkerCommand): String {
        var result = ""
        when (cmd.cmd) {
            WorkerCommand.CMD_REQUEST_AUTH -> result = raw_requestAuth().toString()
            WorkerCommand.CMD_REQUEST_ECHO -> result = raw_requestEcho(cmd.params.firstOrNull())
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