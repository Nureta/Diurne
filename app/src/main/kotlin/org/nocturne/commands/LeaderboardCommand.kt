package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.UserProfile
import org.nocturne.UserProfileQueries
import net.dv8tion.jda.api.interactions.components.buttons.Button

import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.GlobalListeners
import org.nocturne.logic.leveling.LevelingManager
import java.awt.Color


object LeaderboardCommand {
    val COMMAND_NAME = "leaderboard"
    val CHECK_USER_RANK_BOTTON = "check_rank_user_button"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Server Leaderboard"), null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
        GlobalListeners.onButtonInteractionSubscribers[CHECK_USER_RANK_BOTTON] = ::onCheckRankButtonOnInteraction

    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        var leaderboardField = ""
        var sortedUsers = USER_PROFILE.selectUserSortedByLevelDesc().executeAsList()
        var numUsers = Math.min(10, sortedUsers.size)
        for (i in 0..< numUsers) {
            val indexedUser = sortedUsers[i]
            when(i) {
                0 -> leaderboardField += "# Rank `${i+1}`: <@${indexedUser.user_id}> Lvl. ${indexedUser.current_level}\n-# (${indexedUser.experience}/${
                    LevelingManager.nextLevelReq(
                        indexedUser.current_level+1)
                }) XP\n"
                1 -> leaderboardField += "## Rank `${i+1}`: <@${indexedUser.user_id}> Lvl. ${indexedUser.current_level} \n-# (${indexedUser.experience}/${
                    LevelingManager.nextLevelReq(
                        indexedUser.current_level + 1
                    )
                }) XP\n"
                2 -> leaderboardField += "### Rank `${i+1}`: <@${indexedUser.user_id}> Lvl. ${indexedUser.current_level} \n-# (${indexedUser.experience}/${
                    LevelingManager.nextLevelReq(
                        indexedUser.current_level + 1
                    )
                }) XP\n"
                else -> leaderboardField += "Rank `${i+1}`: <@${indexedUser.user_id}> Lvl. ${indexedUser.current_level}  \n-# (${indexedUser.experience}/${
                    LevelingManager.nextLevelReq(
                        indexedUser.current_level + 1
                    )
                }) XP\n"
            }
        }

        val leaderboardEmbed = EmbedBuilder()
            .setTitle("${event.guild?.name}")
            .setDescription(leaderboardField)
            .setColor(Color(102, 171, 212))
            .build()

        event.replyEmbeds(leaderboardEmbed).setEphemeral(false).setActionRow(
                Button.primary(CHECK_USER_RANK_BOTTON,"Check Rank"))
            .queue()


    }
    private fun onCheckRankButtonOnInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != CHECK_USER_RANK_BOTTON) return
        val sortedUsers = USER_PROFILE.selectUserSortedByLevelDesc().executeAsList()
        val userID = event.user.idLong
        val user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()?: return
        var userRankEmbed: MessageEmbed
        if (event.guild!!.getMemberById(userID)!!.isBoosting) {

             userRankEmbed = EmbedBuilder()
                .setColor(Color(115, 138, 255))
                .setAuthor(event.user.name,null,event.user.effectiveAvatarUrl)
                .setTitle("Rank: `${sortedUsers.indexOf(user)+1}`")
                .setDescription("━━━━⊱⋆⊰━━━━\nLvl. ${user.current_level}\n-# ${user.experience}/${LevelingManager.nextLevelReq((user.current_level+1))} [+50% booster Bonus]\n<:Lunaris:1352820067087155232:> **Lunaris** ${user.lunaris}\n━━━━⊱⋆⊰━━━━")
                .build()
        } else {
             userRankEmbed = EmbedBuilder()
                .setColor(Color(115, 138, 255))
                .setAuthor(event.user.name, null, event.user.effectiveAvatarUrl)
                .setTitle("Rank: `${sortedUsers.indexOf(user) + 1}`")
                .setDescription(
                    "━━━━⊱⋆⊰━━━━\nLvl. ${user.current_level}\n-# ${user.experience}/${
                        LevelingManager.nextLevelReq(
                            (user.current_level + 1)
                        )
                    } \n<:Lunaris:1352820067087155232:> **Lunaris** ${user.lunaris}\n━━━━⊱⋆⊰━━━━"
                )
                .build()
        }
        event.replyEmbeds(userRankEmbed).setEphemeral(false).queue()
    }
}

