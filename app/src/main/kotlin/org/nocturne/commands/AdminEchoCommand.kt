package org.nocturne.commands
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.listeners.GlobalListeners


object AdminEchoCommand {
    val COMMAND_NAME = "adminecho"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Echo!")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .addOption(OptionType.STRING, "msg", "What to Echo"), null
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
        val echoOpt = event.getOption("msg") ?: return
        println("ECHO COMMAND - [${event.getChannel()}] ${event.member?.user?.name ?: "Unknown User"}: ${echoOpt}\n")
        event.channel.sendMessage(echoOpt.asString).queue { msg ->
            event.reply("Success").setEphemeral(true).queue()
        }
    }
}