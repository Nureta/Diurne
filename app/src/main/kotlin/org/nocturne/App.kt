package org.nocturne

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.nocturne.listeners.ModalListener
import org.nocturne.listeners.OnMessageSentListener
import org.nocturne.listeners.OnReadyListener
import java.util.*
import javax.xml.crypto.Data


class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}



fun main() {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:test.db")
    Database.Schema.create(driver)

    val dotenv = dotenv {
        directory = "private"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val token = dotenv.get("DISCORD_TOKEN")
    val intents = ArrayList<GatewayIntent>()
    intents.add(GatewayIntent.GUILD_MESSAGES)
    intents.add(GatewayIntent.MESSAGE_CONTENT)
    JDABuilder.createLight(token, intents)
        .addEventListeners(OnReadyListener(), OnMessageSentListener(),ModalListener)
        .build()
}


