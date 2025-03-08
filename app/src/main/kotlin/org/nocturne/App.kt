package org.nocturne

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import org.nocturne.listeners.ModalListener
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
    val intents = ArrayList<GatewayIntent>()
    intents.add(GatewayIntent.GUILD_MESSAGES)
    intents.add(GatewayIntent.MESSAGE_CONTENT)
    JDABuilder.createLight(token, intents)
        .addEventListeners(OnReadyListener(), OnMessageSentListener(),ModalListener)
        .build()
}


