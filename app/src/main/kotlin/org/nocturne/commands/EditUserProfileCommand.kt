package org.nocturne.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.GlobalListeners

object EditUserProfileCommand {
    val COMMAND_NAME = "edituser"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "edit a user's multipliers")
                    .addOption(OptionType.USER,"user","User to edit")
                    .addOption(OptionType.STRING, "multiplier", "Edit the user's exp multiplier")
                , null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {

        val sender = event.member?.user?.id ?: return
        if (!adminUsers.contains(sender)) return

        val multiplierOpt = event.getOption("multiplier") ?: return
        val userOpt = event.getOption("user") ?: return

        USER_PROFILE.updateMultiplier(multiplierOpt.asDouble,userOpt.asLong)

        event.channel.sendMessage("Changed `${userOpt.asString}` multiplier to ${multiplierOpt.asString}").queue {
            event.reply("Success").setEphemeral(true).queue()
        }
    }
}