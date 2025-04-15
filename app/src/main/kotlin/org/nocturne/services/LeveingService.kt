package org.nocturne.services

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.requests.Route.Channels
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.GlobalListeners
import org.nocturne.logic.leveling.LevelingManager.giveExperience
import kotlin.concurrent.timer


object LevelingService {
    var hasInit = false
    val EVENT_NAME = "LEVELING_SERVICE"
    val EXPERIENCE_GAIN = 5.0 //Keep small (this can be op. Counts in milliseconds)



    private fun registerToGlobalListeners() {
        GlobalListeners.onGuildReadyEventSubscribers[EVENT_NAME] = ::onReadyLeveling
    }

    fun init() {
        if (hasInit) return
        hasInit = true
        this.registerToGlobalListeners()
    }

    fun onReadyLeveling(event: GuildReadyEvent) {

        timer(
            "levelTimer",
            period = 20000,
            action = {
                val voiceStatus = event.guild.voiceStates
                for(voiceUser in voiceStatus) {
                    if(voiceUser.member.user.isBot) continue
                    if(voiceUser.isMuted) continue
                    if(voiceUser.isDeafened) continue

                    giveExperience(voiceUser.member.idLong,EXPERIENCE_GAIN,voiceUser)
                }
            }
        )
    }
}