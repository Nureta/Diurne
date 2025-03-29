package org.nocturne

import io.github.cdimascio.dotenv.dotenv
import net.dv8tion.jda.api.JDABuilder
import net.dv8tion.jda.api.requests.GatewayIntent
import net.dv8tion.jda.api.utils.MemberCachePolicy
import org.nocturne.database.DataBaseManager
import org.nocturne.listeners.OnMessageSentListener
import org.nocturne.listeners.GlobalListeners
import org.nocturne.services.LevelingService
import org.nocturne.webserver.WebServer
import org.slf4j.Logger
import org.slf4j.LoggerFactory



class App {
    companion object {
        var logger: Logger = LoggerFactory.getLogger(App::class.java)
    }

    fun start() {
        DataBaseManager.init()

        val dotenv = dotenv {
            directory = "private"
            ignoreIfMalformed = true
            ignoreIfMissing = true
        }
        val token = dotenv.get("DISCORD_TOKEN")
        val keystorePass = dotenv.get("KEYSTORE_PASS")
        WebServer.setAuth(keystorePass)
        WebServer.start()

        val intents = ArrayList<GatewayIntent>()
        intents.add(GatewayIntent.GUILD_MESSAGES)
        intents.add(GatewayIntent.MESSAGE_CONTENT)
        intents.add(GatewayIntent.GUILD_MESSAGE_REACTIONS)
        intents.add(GatewayIntent.GUILD_MEMBERS)
        intents.add(GatewayIntent.GUILD_VOICE_STATES)

        JDABuilder.createDefault(token, intents)
            .addEventListeners(GlobalListeners, OnMessageSentListener())
            .setMemberCachePolicy(MemberCachePolicy.VOICE)
            .build()
    }


}

    fun main() {
        val app = App()
        app.start()

    }


