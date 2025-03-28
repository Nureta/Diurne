package org.nocturne.webserver

import io.ktor.network.tls.certificates.*
import io.ktor.server.netty.*
import io.ktor.server.routing.*
import io.ktor.server.application.*
import io.ktor.server.response.*
import io.ktor.server.engine.*
import org.slf4j.LoggerFactory
import java.io.File

fun Application.module() {
    routing {
        get("/") {
            call.respondText("Hello, world!")
        }
    }
}
object WebServer {
    private var AUTH_PASS = ""
    var serverThread: Thread? = null
    fun start() {
        val server = embeddedServer(Netty,
            applicationEnvironment {
                log = LoggerFactory.getLogger("ktor")
            }, {
                envConfig()
            }, module = Application::module).start(wait = false)
        // serverThread!!.start()
    }

    fun stop() {
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
}

