package org.nocturne.webserver

import io.ktor.http.*
import io.ktor.network.tls.certificates.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.engine.*
import io.ktor.server.request.*
import io.ktor.server.websocket.*
import io.ktor.websocket.*
import kotlinx.coroutines.delay
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromStream
import org.slf4j.LoggerFactory
import java.io.File
import java.util.*
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val logger = LoggerFactory.getLogger(WebServer::class.java)

fun Application.module() {
    install(WebSockets) {
        pingPeriod = 5.seconds
        timeout = 5.minutes
    }
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
        get("/task") {
            val task = WebServer.getTask()
            if (task == null) call.respond(HttpStatusCode.NoContent)
            else {
                call.respondText(task.toString())
            }
        }
        post("/result/string") {
            val genericCmdData = call.receiveText()
            val genericResult = Json.decodeFromString<GenericStringResult>(genericCmdData)
            ComputeJobManager.genericStringResult(UUID.fromString(genericResult.id), genericResult.result)
        }
        webSocket("/tasksocket") {
            WebServer.handleTaskSocket(this)
        }
    }
}

object WebServer {
    private var AUTH_PASS = ""
    var server:  EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    fun start() {
        server = embeddedServer(Netty,
            applicationEnvironment {
                log = LoggerFactory.getLogger("ktor")
            }, {
                envConfig()
            }, module = Application::module).start(wait = false)
    }

    fun stop() {
        server?.stop(1000, 5000)
        server = null
    }

    private fun ApplicationEngine.Configuration.envConfig() {

        val keyStoreFile = File("private/server-keystore.jks")
        val keyStore = buildKeyStore {
            certificate("ktorAlias") {
                password = AUTH_PASS
                domains = listOf("127.0.0.1", "0.0.0.0", "localhost")
            }
        }
        keyStore.saveToFile(keyStoreFile, AUTH_PASS)
        connector {
            port = 15657
        }
        sslConnector(
            keyStore = keyStore,
            keyAlias = "ktorAlias",
            keyStorePassword = { AUTH_PASS.toCharArray() },
            privateKeyPassword = { AUTH_PASS.toCharArray() }) {
            port = 8443
            keyStorePath = keyStoreFile
        }
    }

    fun setAuth(pass: String) {
        AUTH_PASS = pass
    }


    fun getTask(): JsonObject? {
        val task = ComputeJobManager.getComputeJob() ?: return null
        return task.toJson()
    }

    suspend fun handleTaskSocket(socket: WebSocketSession) {
        logger.info("Task Socket Connected!")
        while (true) {
            val jobs = ComputeJobManager.commandQueue.size
            socket.send("$jobs")
            delay(1000)
        }
    }


}

