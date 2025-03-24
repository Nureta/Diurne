package org.nocturne

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.nocturne.listeners.GlobalListeners
import org.nocturne.listeners.OnMessageSentListener
import org.nocturne.sockets.SocketManager
import org.slf4j.Logger
import org.slf4j.LoggerFactory


class App {
    companion object {
        var logger: Logger = LoggerFactory.getLogger(App::class.java)
    }


    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:test.db")

    try {
        NocturneDB.Schema.create(driver)
    } catch (ignored: Exception) {}


    val dotenv = dotenv {
        directory = "private"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val token = dotenv.get("DISCORD_TOKEN")
    val keystorePass = dotenv.get("KEYSTORE_PASS")
    SocketManager.socketAuth = dotenv.get("AUTH_PASS")
    SocketManager.start(keystorePass, 15656)

    val intents = ArrayList<GatewayIntent>()
    intents.add(GatewayIntent.GUILD_MESSAGES)
    intents.add(GatewayIntent.MESSAGE_CONTENT)
    JDABuilder.createLight(token, intents)
        .addEventListeners(GlobalListeners, OnMessageSentListener())
        .build()
}


