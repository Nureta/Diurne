package org.nocturne.services

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.Route.Channels
import org.nocturne.listeners.GlobalListeners
import kotlin.concurrent.timer







object LevelingService {
    var hasInit = false
    val EVENT_NAME = "LEVELING_SERVICE"



    private fun registerToGlobalListeners() {
        GlobalListeners.onGuildReadyEventSubscribers[EVENT_NAME] = ::onReadyLeveling
    }

    fun init() {
        if (hasInit) return
        hasInit = true
        this.registerToGlobalListeners()
    }



    fun onReadyLeveling(event: GuildReadyEvent) {
        val voiceChannels = event.guild.voiceChannels
        for(channels in voiceChannels) {
            val members = event.jda.getVoiceChannelById(channels.idLong)?.members ?: continue
            for (person in members) {
                println("!!! ${person.user.name}")
            }
        }
        timer(
            "levelTimer",
            period = 5000,
            action = {

            }
        )

    }




}