package org.nocturne.listeners
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.nocturne.logic.leveling.LevelingManager
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.slf4j.LoggerFactory
import kotlin.time.DurationUnit
import kotlin.time.toDuration

class OnMessageSentListener : ListenerAdapter() {
    val EXP_COOLDOWN = (10).toDuration(DurationUnit.SECONDS).inWholeMilliseconds
    val EXPERIENCE_GAIN = 5
    val NITRO_BONUS = 1.5

    val logger = LoggerFactory.getLogger(OnMessageSentListener::class.java)
    override fun onMessageReceived(event: MessageReceivedEvent) {
        val userID = event.author.idLong
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()
        val currentTime = System.currentTimeMillis()

        if (event.author.isBot) return
        logger.debug("[{}] {}: {}\n", event.getChannel(), event.author, event.message.contentDisplay)

        //Inserts unique user if no discord user id is present in the table
        if (user == null) {
            USER_PROFILE.insertUser(userID, 1, 0.0, 0, 1.0)
            return
        }

        logger.info("USER: ${user.user_id}\nLEVEL: ${user.current_level}\nEXP: ${user.experience}\nCOOLDOWN: ${(currentTime-(user.cooldown)).toDuration(DurationUnit.MILLISECONDS).inWholeSeconds}\nMULTIPLIER: ${user.multiplier}\n")

        if ((currentTime - user.cooldown) < EXP_COOLDOWN) return
        USER_PROFILE.updateCooldown(currentTime,user.user_id)

        when(event.member?.isBoosting) {
            true -> USER_PROFILE.updateExperience(user.experience + (EXPERIENCE_GAIN*(NITRO_BONUS * (user.multiplier))),user.user_id)
            else -> USER_PROFILE.updateExperience(user.experience + (EXPERIENCE_GAIN* user.multiplier),user.user_id)
        }


        if (!LevelingManager.checkLevel(userID)) return

        val levelEmbed = EmbedBuilder()
            .setTitle("Leveled up!")
            .setDescription("**${event.author.name}** has leveled up to ${user.current_level+1}\nNext level requires `${LevelingManager.nextLevelReq(user.current_level+1)}`")
            .setFooter("IN BETA, THIS WILL BE WIPED")
            .build()

        USER_PROFILE.updateLevel(user.current_level+1,userID)
        USER_PROFILE.updateExperience(0.0,userID)
        event.message.replyEmbeds(levelEmbed).queue()

    }


}