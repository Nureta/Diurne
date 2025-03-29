package org.nocturne.logic.leveling

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.GuildVoiceState
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
    val LEVEL_CONSTANT = 300
    val LEVEL_COEFFICENT = 125
    val NITRO_BONUS = 1.5


    fun checkLevel(userID: Long): Boolean {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull() ?: return false
        val selectedUserEXP = user.experience
        val nextLevelReq = user.current_level.let { nextLevelReq(it) }
        if (selectedUserEXP < nextLevelReq) return false
        return true
    }
    private fun levelingBump(x: Double, coefficient: Double, offset: Double): Double {
        return ((coefficient* log(x, 2.11)) /(exp(-1*(x-offset))+1 ))
    }

    fun nextLevelReq(level: Long): Long{
        var reqEXP = 0L
        reqEXP = ((LEVEL_COEFFICENT * (level
                + levelingBump(level.toDouble(),15.0,10.0)
                + levelingBump(level.toDouble(),100.0,50.0)
                + levelingBump(level.toDouble(),200.0,100.0)
                + levelingBump(level.toDouble(),400.0,200.0)
                ))
                + LEVEL_CONSTANT).toLong()
        return reqEXP
    }

    fun <T> giveExperience(userID: Long, expGain: Double, event: T) {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull() ?: return
        var userName = ""
        var booster = false
        var avatar = ""

        when(event) {
            is MessageReceivedEvent -> {
                booster = event.member?.isBoosting ?: return
                userName = event.member!!.user.name
                avatar = event.author.effectiveAvatarUrl
            }
            is GuildVoiceState -> {
                booster = event.member.isBoosting
                userName = event.member.user.name
                avatar = event.member.user.effectiveAvatarUrl

            }
            null -> return
        }
            when (booster) {
                true -> USER_PROFILE.updateExperience(
                    (user.experience + (expGain * (NITRO_BONUS * (user.multiplier)))).toLong(),
                    user.user_id
                )

                else -> USER_PROFILE.updateExperience((user.experience + (expGain * user.multiplier)).toLong(), user.user_id)
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
        USER_PROFILE.updateExperience(0, userID)
        when(event) {
            is MessageReceivedEvent  -> {
                event.message.replyEmbeds(levelEmbed).queue()
            }
            is GuildVoiceState -> {
                event.member.voiceState?.channel?.idLong?.let { event.guild.getVoiceChannelById(it) }?.sendMessageEmbeds(levelEmbed)?.queue()
            }
        }
    }
}