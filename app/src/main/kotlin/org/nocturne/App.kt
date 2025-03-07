package org.nocturne

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.GatewayIntent
import java.util.*


class App {
    val greeting: String
        get() {
            return "Hello World!"
        }
}

class MessageReceiveListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        System.out.printf(
            "[%s] %#s: %s\n",
            event.channel,
            event.author,
            event.message.contentDisplay
        )
    }
}

fun main() {
    val dotenv = dotenv {
        directory = "private"
        ignoreIfMalformed = true
        ignoreIfMissing = true
    }
    val token = dotenv.get("DISCORD_TOKEN")
    JDABuilder.createLight(token, EnumSet.of(GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT))
        .addEventListeners(MessageReceiveListener())
        .build()
}
