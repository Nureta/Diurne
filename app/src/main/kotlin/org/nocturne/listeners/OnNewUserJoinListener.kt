package org.nocturne.listeners

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.OnMessageReactedListener.EVENT_NAME
import org.nocturne.listeners.OnMessageReactedListener.hasInit
import org.nocturne.logic.leveling.LevelingManager.CheckLevelToAssign

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
        if (event.user.isBot) return
        val user = USER_PROFILE.selectUserByUserId(event.member.idLong).executeAsOneOrNull()
        println("Someone Joined!")
        if (user == null) {
            USER_PROFILE.insertUser(event.member.idLong, 1, 0, 0, 1.0,0)
            return
        }
        CheckLevelToAssign(user.current_level.toInt(),event.member.user,event.guild)
    }
}