package org.nocturne

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.nocturne.listeners.OnMessageSentListener
import org.nocturne.listeners.OnReadyListener
import java.util.*


class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

fun main() {
    val dotenv = dotenv {
        directory = "private"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val token = dotenv.get("DISCORD_TOKEN")
    JDABuilder.createLight(token, ArrayList<GatewayIntent>())
        .addEventListeners(OnReadyListener(), OnMessageSentListener())
        .build()
}
