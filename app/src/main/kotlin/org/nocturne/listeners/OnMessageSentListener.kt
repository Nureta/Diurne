package org.nocturne.listeners
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.utils.JDALogger
import org.nocturne.UserProfile
import org.nocturne.UserProfileQueries
import org.nocturne.database.DataBaseManager
import org.nocturne.sockets.SocketManager
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class OnMessageSentListener : ListenerAdapter() {
    val USER_PROFILES = UserProfileQueries(DataBaseManager.driver)
    val EXP_COOLDOWN = (30).toDuration(DurationUnit.SECONDS).inWholeMilliseconds

    val logger = LoggerFactory.getLogger(OnMessageSentListener::class.java)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val guild_id  = event.guild.idLong
        val user_id = event.author.idLong
        val user = USER_PROFILES.selectUserById(user_id).executeAsOneOrNull()
        val currentTime = System.currentTimeMillis()

        if (event.author.isBot) return
        logger.debug("[{}] {}: {}\n", event.getChannel(), event.author, event.message.contentDisplay)

        //Inserts unique user if no discord user id is present in the table
        if (user == null) {
            USER_PROFILES.insertUser(user_id, guild_id, 1, 0.0, 0, 1.0)
            //todo check for unique guild_id
            return
        }

        println("USER: ${user.user_id}\nGUILD: ${user.guild_id}\nLEVEL: ${user.current_level}\nEXP: ${user.experience}\nCOOLDOWN: ${(currentTime-(user.cooldown)).toDuration(DurationUnit.MILLISECONDS).inWholeSeconds}\nMULTIPLIER: ${user.multiplier}\n")
        if ((currentTime - user.cooldown) < EXP_COOLDOWN) return
        USER_PROFILES.updateCooldown(currentTime,user.user_id)
        USER_PROFILES.updateExperience(user.experience + (50* user.multiplier),user.user_id)



    }

}