package org.nocturne.webserver

import org.nocturne.sockets.ClientWorkerConnection
import org.nocturne.sockets.SSLUtil
import org.nocturne.sockets.SocketManager
import org.slf4j.LoggerFactory
import java.net.Socket
import javax.net.ssl.SSLServerSocket

object NotificationSocketManager {
    val logger = LoggerFactory.getLogger(NotificationSocketManager::class.java)
    var socketAuth = ""
    var certAuth = ""
    var isRunning = false
        private set

    var serverSocket: SSLServerSocket? = null
    var connectionThread: Thread? = null
    var connectedSockets = ArrayList<Socket>()


    fun setAuth(certAuth: String, socketAuth: String) {
        this.socketAuth = socketAuth
    }

    fun start(port: Int): Boolean {
        if (serverSocket != null) {
            logger.error("SSL Server Socket already exists! Please stop and/or set to null.")
            return false
        }
        if (connectionThread != null) {
            logger.error("Connection Thread already exists! Please stop it!")
            return false
        }

        try {
            serverSocket = SSLUtil.createSSLSocket(certAuth, port)
        } catch (e: Exception) {
            logger.error("Error creating SSL Socket.\n${e.message}\n${e.stackTraceToString()}")
            return false
        }
        serverSocket!!.needClientAuth = true
        logger.info("🔒Secure Server is running on port $port...")
        connectionThread = Thread {
        }
        isRunning = true
        return true
    }

    fun handleSocketConnections() {

    }

}