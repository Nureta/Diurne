package org.nocturne.sockets

import net.dv8tion.jda.api.requests.RestRateLimiter.Work
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

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

    val writeLock = Object()
    fun start() {
        writer = PrintWriter(socket.getOutputStream(), false)
        reader = BufferedReader(InputStreamReader(socket.getInputStream()))
        requestAuth()
    }

    fun requestAuth(): Boolean {
        writeCommand(WorkerProtocol.CMD_REQUEST_AUTH)
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

    /**
     * @see WorkerProtocol
     * Will write a normal command with params
     */
    private fun writeCommand(cmd: String, params: List<String>) {
        assertInit()
        var sentCmd = "${WorkerProtocol.COMMAND_PREFIX}[${cmd}]"
        if (params.isNotEmpty()) {
            sentCmd += "@param["
            for (p in params) {
                sentCmd += p + ","
            }
            sentCmd = cmd.removeSuffix(",")
            sentCmd += "]"
        }
        synchronized(writeLock) {
            logger.debug("Sending Command ${sentCmd}")
            writer.println(sentCmd)
            writer.flush()
        }
    }
    private fun writeCommand(cmd: String) {
        writeCommand(cmd, emptyList())
    }

    private fun getReplyData(): String {
        var result = reader.readLine()
        if (!result.startsWith(WorkerProtocol.REPLY_PREFIX)) {
            logger.warn("Received BAD reply data, discarding all!")
            clearReadBuffer()
            return ""
        }
        result = result.removePrefix(WorkerProtocol.REPLY_PREFIX)
        while (!result.endsWith(WorkerProtocol.REPLY_SUFFIX)) {
            result += reader.readLine()
        }
        if (result.endsWith(WorkerProtocol.REPLY_SUFFIX)) {
            result = result.removeSuffix(WorkerProtocol.REPLY_SUFFIX)
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

    /**
     * Ensures that writer/reader are initialized
     */
    private fun assertInit() {
        assert(this::writer.isInitialized)
        assert(this::reader.isInitialized)
    }
}