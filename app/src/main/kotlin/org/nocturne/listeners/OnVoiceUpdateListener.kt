package org.nocturne.listeners

import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.nocturne.logic.leveling.LevelingManager
import java.util.HashMap

object OnVoiceUpdateListener : ListenerAdapter() {
    private const val EVENT_NAME = "VOICE_UPDATE"
    private val USER_TIME_MAP = HashMap<Long,Long>()
    private const val EXPERIENCED_COEFFICIENT = 0.01 //Keep small (this can be op. Counts in milliseconds)
    var hasInit = false


//    private fun registerToGlobalListeners() {
//        GlobalListeners.onGuildVoiceUpdateSubscribers[EVENT_NAME] = ::onVoiceUpdate
//    }

//    fun init() {
//        if (hasInit) return
//        hasInit = true
//        this.registerToGlobalListeners()
//    }



    fun onVoiceUpdate(event: GuildVoiceUpdateEvent) {
        println("WROKING")
        var timeElapsed = 0L
        val userID = event.member.user.idLong
        if(event.channelJoined == null) {
            timeElapsed = System.currentTimeMillis() - USER_TIME_MAP.get(userID)!!.toLong()
            LevelingManager.giveExperience(userID,(timeElapsed * EXPERIENCED_COEFFICIENT),event)
            USER_TIME_MAP.remove(userID)
            return
        }

        USER_TIME_MAP[userID] = System.currentTimeMillis()
    }
}