package org.nocturne.sockets
import okhttp3.internal.closeQuietly
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLServerSocketFactory
import org.slf4j.LoggerFactory
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket


object SocketManager {
    val logger = LoggerFactory.getLogger(SocketManager::class.java)
    var socketAuth = ""
    var serverSocket: SSLServerSocket? = null
    var connectionThread: Thread? = null
    var clientConnection: ClientWorkerConnection? = null

    /**
     * Start SSL Socket Server, start thread to handle SSL Communication
     * @param keypass - Password to keystore to init SSL with.
     */
    fun start(keypass: String, port: Int): Boolean {
        if (serverSocket != null) {
            logger.error("SSL Server Socket already exists! Please stop and/or set to null.")
            return false
        }
        try {
            serverSocket = SSLUtil.createSSLSocket(keypass, port)
        } catch (e: Exception) {
            logger.error("Error creating SSL Socket.\n${e.message}\n${e.stackTraceToString()}")
            return false
        }

        serverSocket!!.needClientAuth = true
        logger.info("🔒Secure Server is running on port $port...")
        connectionThread = Thread {
            socketConnectionHandler()
        }
        connectionThread!!.start()
        return true
    }

    /**
     * Accepts socket connections & pass over to handler thread.
     */
    fun socketConnectionHandler() {
        val mySocket: SSLServerSocket? = serverSocket
        if (mySocket == null) {
            logger.error("SSL Communication Handler - Socket not initialized!")
            return
        }
        while (true) {
            try {
                if (mySocket.isClosed) break
                val clientSocket = mySocket.accept()
                logger.info("Client Connected! - ${clientSocket.inetAddress.hostAddress}")
                Thread {
                    clientConnection = ClientWorkerConnection(clientSocket)
                    clientConnection!!.start()
                }.start()
            } catch (e: Exception) {
                clientConnection?.isRunning = false
                logger.error("Socket Connection Handler Error: ${e.message}\n${e.stackTraceToString()}")
            }
        }
        logger.info("Closing Socket Connection Handler.")
    }

    fun socketClientCommunicationHandler(clientSocket: Socket) {
        try {
            val writer = PrintWriter(clientSocket.getOutputStream(), true)
            val reader = BufferedReader(InputStreamReader(clientSocket.getInputStream()))
            while (true) {
                val result = reader.readLine()
                if (result.lowercase().trim() == "exit") {
                    clientSocket.close()
                    return
                }
                writer.println("ACK: $result")
            }
        } catch (e: Exception) {
            logger.warn("Client ${clientSocket.inetAddress} Disconnected  ${e.stackTraceToString()}")
        } finally {
            clientSocket.close()
        }
    }
}