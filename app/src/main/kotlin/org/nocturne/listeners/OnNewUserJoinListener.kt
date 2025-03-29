package org.nocturne.listeners

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.OnMessageReactedListener.EVENT_NAME
import org.nocturne.listeners.OnMessageReactedListener.hasInit

object OnNewUserJoinListener {
    private fun registerToGlobalListeners() {
        GlobalListeners.onGuildMemberJoinEventSubscribers[EVENT_NAME] = ::onNewUserListener

    }
    fun init() {
        if (hasInit) return
        hasInit = true
        this.registerToGlobalListeners()
    }
    fun onNewUserListener(event: GuildMemberJoinEvent) {
        val user = USER_PROFILE.selectUserByUserId(event.member.idLong)
        if (user == null) {
            USER_PROFILE.insertUser(event.member.idLong, 1, 0, 0, 1.0,0)
            return
        }
    }
}