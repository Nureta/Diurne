package org.nocturne.logic.leveling

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.OnMessageSentListener
import org.slf4j.LoggerFactory
import kotlin.math.exp
import kotlin.math.log

object LevelingManager {
    val LEVEL_CONSTANT = 100
    val LEVEL_COEFFICENT = 50
    val LEVEL_OFFSET = 50
    val NITRO_BONUS = 1.5

    val logger = LoggerFactory.getLogger(OnMessageSentListener::class.java)


    fun checkLevel(userID: Long): Boolean {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull() ?: return false
        val selectedUserEXP = user.experience
        val nextLevelReq = user.current_level.let { nextLevelReq(it) }
        if (selectedUserEXP < nextLevelReq) return false
        return true
    }

    fun nextLevelReq(level: Long): Double{
        var reqEXP = 0.0
        reqEXP = (LEVEL_COEFFICENT * (level + ((55* log(level.toDouble(), 2.11)) /(exp(-1*(level.toDouble()-LEVEL_OFFSET))+1 )) )) + LEVEL_CONSTANT
        return reqEXP
    }

    fun <T> giveExperience(userID: Long, expGain: Double, event: T) {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull() ?: return
        var userName = ""
        var booster = false
        var avatar = ""
        var banner = ""
        when(event) {
            is MessageReceivedEvent -> {
                booster = event.member?.isBoosting ?: return
                userName = event.member!!.user.name
                avatar = event.author.effectiveAvatarUrl
            }
            is GuildVoiceUpdateEvent -> {
                booster = event.member.isBoosting
                userName = event.member.user.name

            }
            null -> return
        }
            when (booster) {
                true -> USER_PROFILE.updateExperience(
                    user.experience + (expGain * (NITRO_BONUS * (user.multiplier))),
                    user.user_id
                )

                else -> USER_PROFILE.updateExperience(user.experience + (expGain * user.multiplier), user.user_id)
            }

        if (!checkLevel(userID)) return

        val levelEmbed = EmbedBuilder()
            .setTitle("Leveled up!")
            .setAuthor(userName,null,avatar)
            .setDescription(
                "**${userName}** has leveled up to ${user.current_level + 1}\nNext level requires `${
                    nextLevelReq(
                        user.current_level + 1
                    )
                }`"
            )
            //todo FIX LEVELING ROUNDING
            .setFooter("IN BETA, THIS WILL BE WIPED")
            .build()

        USER_PROFILE.updateLevel(user.current_level + 1, userID)
        USER_PROFILE.updateExperience(0.0, userID)
        when(event) {
            is MessageReceivedEvent  -> {
                event.message.replyEmbeds(levelEmbed).queue()
            }
            is GuildVoiceUpdateEvent -> {
                if (event.channelLeft == null) return
                event.guild.getVoiceChannelById(event.channelLeft!!.idLong)?.sendMessage("<@${userID}>")?.queue()
                event.guild.getVoiceChannelById(event.channelLeft!!.idLong)?.sendMessageEmbeds(levelEmbed)?.queue()
            }
        }
    }
}