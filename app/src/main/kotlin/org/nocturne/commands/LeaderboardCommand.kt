package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.UserProfile
import org.nocturne.UserProfileQueries
import net.dv8tion.jda.api.interactions.components.buttons.Button
import org.nocturne.commands.ConfessionCreateCommand.CONFESSION_BUTTON_NEW

import org.nocturne.database.DataBaseManager
import org.nocturne.listeners.GlobalListeners
import java.awt.Color


object LeaderboardCommand {
    val COMMAND_NAME = "leaderboard"
    val USER_PROFILE = UserProfileQueries(DataBaseManager.driver)
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
                0 -> leaderboardField += "# ${i+1}: <@${indexedUser.user_id}>: ${indexedUser.experience} \n"
                1 -> leaderboardField += "## ${i+1}: <@${indexedUser.user_id}>: ${indexedUser.experience} \n"
                2 -> leaderboardField += "### ${i+1}: <@${indexedUser.user_id}>: ${indexedUser.experience} \n"
                else -> leaderboardField += "${i+1}: <@${indexedUser.user_id}>: ${indexedUser.experience} \n"
            }
        }

        val leaderboardEmbed = EmbedBuilder()
            .setTitle("${event.guild?.name}")
            .setDescription(leaderboardField)
            .setColor(Color.BLACK)

            .build()

        event.replyEmbeds(leaderboardEmbed).setEphemeral(false).setActionRow(
                Button.primary(CHECK_USER_RANK_BOTTON,"Check Rank"))
            .queue()


    }
    private fun onCheckRankButtonOnInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != CHECK_USER_RANK_BOTTON) return
        val sortedUsers = USER_PROFILE.selectUserSortedByLevelDesc().executeAsList()
        val userRankEmbed = EmbedBuilder()
            .setColor(Color.BLUE)
            .setTitle("${event.user.name} RANK")
            .setDescription("${sortedUsers.indexOf(USER_PROFILE.selectUserById(event.user.idLong).executeAsOne())+1}: ${USER_PROFILE.selectUserById(event.user.idLong).executeAsOne().experience}")
            .build()
        event.replyEmbeds(userRankEmbed).setEphemeral(false).queue()
    }
}

//${sortedUsers.indexOf(USER_PROFILE.selectUserById(event.user.idLong).executeAsOne())}: ${USER_PROFILE.selectUserById(event.user.idLong).executeAsOne().experience}