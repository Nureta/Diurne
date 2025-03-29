package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.nocturne.UserProfile
import org.nocturne.listeners.GlobalListeners
import org.nocturne.logic.leveling.LevelingManager
import java.awt.Color
import org.nocturne.database.DataBaseManager.USER_PROFILE

object CheckRankCommand {
    val COMMAND_NAME = "rank"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Checks the user's rank")
                    .addOption(OptionType.USER,"user","User to check", false)
                ,null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val sortedUsers = USER_PROFILE.selectUserSortedByLevelDesc().executeAsList()
        val userID = event.user.idLong
        var user: UserProfile
        val userOpt = event.getOption("user")?.asMember

        if (userOpt != null) {
            user = USER_PROFILE.selectUserByUserId(userOpt.idLong).executeAsOneOrNull()!!
            val userRankEmbed = EmbedBuilder()
                .setColor(Color(115, 138, 255))
                .setAuthor(event.user.name,null,userOpt.effectiveAvatarUrl)
                .setTitle("Rank: `${sortedUsers.indexOf(user)+1}`")
                .setDescription("## Lvl. ${user.current_level}\n-# ${user.experience}/${LevelingManager.nextLevelReq((user.current_level+1))}")
                .build()
            event.replyEmbeds(userRankEmbed).queue()
            return
        }

       user = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()?: return
        val userRankEmbed = EmbedBuilder()
            .setColor(Color(115, 138, 255))
            .setAuthor(event.user.name,null,event.member?.user?.effectiveAvatarUrl)
            .setTitle("${event.user.name} Rank: `${sortedUsers.indexOf(user)+1}`")
            .setDescription("╔═*.·:·.✧ ✦ ✧.·:·.*═╗ \n### Lvl. ${user.current_level}\n-# ${user.experience}/${LevelingManager.nextLevelReq((user.current_level+1))}\n╚═*.·:·.✧ ✦ ✧.·:·.*═╝ ")
            .build()
        event.replyEmbeds(userRankEmbed).queue()
    }
}