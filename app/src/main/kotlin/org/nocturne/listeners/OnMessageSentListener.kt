package org.nocturne.listeners
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.nocturne.logic.leveling.LevelingManager
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.slf4j.LoggerFactory
import java.util.logging.Level
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class OnMessageSentListener : ListenerAdapter() {
    val EXP_COOLDOWN = (10).toDuration(DurationUnit.SECONDS).inWholeMilliseconds


    val logger = LoggerFactory.getLogger(OnMessageSentListener::class.java)

    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userID = event.author.idLong
        val EXPERIENCE_GAIN = 30.0
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()
        val currentTime = System.currentTimeMillis()

        if (event.author.isBot) return
        logger.debug("[{}] {}: {}\n", event.channel, event.author, event.message.contentDisplay)

        //Inserts unique user if no discord user id is present in the table
        if (user == null) {
            USER_PROFILE.insertUser(userID, 1, 0, 0, 1.0,0)
            return
        }
        //todo, HAVE THIS THINGY FOR FIRST USER JOINS TOO! also this weird ASS bug where you gotta send multiple requests before you are inserted into the db


        logger.info("USER: ${user.user_id}\nLEVEL: ${user.current_level}\nEXP: ${user.experience}\nCOOLDOWN: ${(currentTime-(user.cooldown)).toDuration(DurationUnit.MILLISECONDS).inWholeSeconds}\nMULTIPLIER: ${user.multiplier}\n")

        if ((currentTime - user.cooldown) < EXP_COOLDOWN) return
        USER_PROFILE.updateCooldown(currentTime,user.user_id)
        LevelingManager.giveExperience(userID,EXPERIENCE_GAIN,event)





    }


}