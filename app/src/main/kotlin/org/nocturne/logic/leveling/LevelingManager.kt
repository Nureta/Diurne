package org.nocturne.logic.leveling

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Guild
import net.dv8tion.jda.api.entities.GuildVoiceState
import net.dv8tion.jda.api.entities.Member
import net.dv8tion.jda.api.entities.User
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.OnMessageSentListener
import org.slf4j.LoggerFactory
import kotlin.math.exp
import kotlin.math.log

object LevelingManager {
    val LEVEL_ROLES = HashMap<Int,Long>()
    val LEVEL_CONSTANT = 300
    val LEVEL_COEFFICENT = 125
    val NITRO_BONUS = 1.5
    val MAX_LEVEL = 100
    val ROLE_ID_ARRAY = listOf<Long>(
        1359273043578458142, //level 5
        1327153116667449404, //level 10
        1327153151731826748, //level 15
        1327153242542571570, //level 20
        1331912386432471092, //level 30
        1331912651802148906, //level 40
        1353472106733764658, //level 50
        1353470323592400906, //level 60
        1353470331670495343, //level 70
        1353470335185322124, //level 80
        1353470326775746560, //level 90
        1353472243912671395  //level 100
    )


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
    fun CheckLevelToAssign(level: Int, user: User, guild: Guild) {
        //LEVEL_ROLES
        when (level) {
            in 5..9 -> guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[0])?: return).queue()
            in 10..14 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[1]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[0])?: return).queue()
            }
            in 15..19 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[2]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[1])?: return).queue()
            }
            in 20..29 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[3]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[2])?: return).queue()

            }
            in 30..39 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[4]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[3])?: return).queue()
            }
            in 40..49 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[5]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[4])?: return).queue()
            }
            in 50..59 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[6]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[5])?: return).queue()
            }
            in 60..69 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[7]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[6])?: return).queue()
            }
            in 70..79 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[8]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[7])?: return).queue()
            }
            in 80..89 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[9]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[8])?: return).queue()
            }
            in 90..99 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[10]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[9])?: return).queue()
            }
            100 -> {
                guild.addRoleToMember(user, guild.getRoleById(ROLE_ID_ARRAY[11]) ?: return).queue()
                guild.removeRoleFromMember(user,guild.getRoleById(ROLE_ID_ARRAY[10])?: return).queue()
            }
        }
    }

    fun nextLevelReq(level: Long): Long{
        var reqEXP = 0L
        reqEXP = ((LEVEL_COEFFICENT * (level
                + levelingBump(level.toDouble(),10.0,10.0)
                + levelingBump(level.toDouble(),5.0,25.0)
                + levelingBump(level.toDouble(),55.0,50.0)
                + levelingBump(level.toDouble(),150.0,100.0)
                ))
                + LEVEL_CONSTANT).toLong()
        return reqEXP
    }

    fun <T> giveExperience(userID: Long, expGain: Double, event: T) {
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull() ?: return
        var userName = ""
        var booster = false
        var avatar = ""
        if (user.current_level >= MAX_LEVEL) return

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
            .setFooter("IN BETA, THIS WILL BE WIPED")
            .build()

        USER_PROFILE.updateLevel(user.current_level + 1, userID)
        USER_PROFILE.updateExperience(0, userID)

        when(event) {
            is MessageReceivedEvent  -> {
                CheckLevelToAssign(user.current_level.toInt(),event.member!!.user,event.guild)
                event.message.replyEmbeds(levelEmbed).queue()
            }
            is GuildVoiceState -> {
                CheckLevelToAssign(user.current_level.toInt(),event.member.user,event.guild)
                event.member.voiceState?.channel?.idLong?.let { event.guild.getVoiceChannelById(it) }?.sendMessageEmbeds(levelEmbed)?.queue()
            }
        }
    }
}