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
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonObject
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileOutputStream
import java.nio.file.Files
import java.nio.file.Paths
import java.util.UUID
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Duration.Companion.minutes
import kotlin.time.Duration.Companion.seconds

val logger = LoggerFactory.getLogger(WebServer::class.java)

@OptIn(ExperimentalEncodingApi::class)
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
            call.respond(HttpStatusCode.OK)
        }

        post("/result/quote") {
            val params = call.queryParameters
            val id = params.get("id")
            val filename = params.get("filename")
            if (filename == null) {
                call.respond(HttpStatusCode.BadRequest, "Specify Query Param")
                return@post
            }
            val data = Base64.decode(call.receiveText())
            val resultPath = WebServer.saveCacheFile(filename, data)
            val result = hashMapOf("filename" to filename, "filepath" to resultPath)
            ComputeJobManager.genericMapResult(UUID.fromString(id), result)
            call.respond(HttpStatusCode.OK)
        }
        post("/result/toxic") {
            val params = call.queryParameters
            val id = params.get("id")
            val jsonData = call.receiveText()
            val data = Json.parseToJsonElement(jsonData)
            val neutral = data.jsonObject.get("neutral").toString()
            val toxic = data.jsonObject.get("toxic").toString().removeSurrounding("\"")
            val result = hashMapOf("neutral" to neutral, "toxic" to toxic)
            ComputeJobManager.genericMapResult(UUID.fromString(id), result)
        }

        webSocket("/tasksocket") {
            try {
                WebServer.handleTaskSocket(this)
            } catch (e: Exception) {
                logger.info("Socket disconnected")
                WebServer.socketConnections.remove(this)
            }
        }
    }
}

object WebServer {
    private var CERT_PASS = ""
    private var CERT_PATH = ""

    var server:  EmbeddedServer<NettyApplicationEngine, NettyApplicationEngine.Configuration>? = null
    var socketConnections = ArrayList<WebSocketSession>()
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
        connector {
            port = 15656
        }
    }

    fun setAuth(pass: String, path: String) {
        CERT_PASS = pass
        CERT_PATH = path
    }


    fun getTask(): JsonObject? {
        val task = ComputeJobManager.getComputeJob() ?: return null
        return task.toJson()
    }

    suspend fun handleTaskSocket(socket: WebSocketSession) {
        logger.info("Task Socket Connected!")
        socketConnections.add(socket)
        while (true) {
            val jobs = ComputeJobManager.numberOfJobs()
            socket.send("$jobs")
            delay(1000)
        }
    }

    fun hasSocketConnection(): Boolean {
        return socketConnections.isNotEmpty()
    }

    fun notifyRandomSocket() {
        runBlocking {
            val jobs = ComputeJobManager.numberOfJobs()
            socketConnections.random().send("$jobs")
        }
    }

    fun saveCacheFile(filename: String, data: ByteArray): String {
        Files.createDirectories(Paths.get("./cache"))
        val filepath = "./cache/${filename}"
        val fos = FileOutputStream(filepath)
        fos.write(data)
        fos.close()
        return filepath
    }

}

