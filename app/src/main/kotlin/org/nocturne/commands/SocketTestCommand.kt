package org.nocturne.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import okhttp3.internal.wait
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.listeners.GlobalListeners
import org.nocturne.webserver.ComputeJobManager
import org.nocturne.webserver.WebServer
import org.slf4j.LoggerFactory

object SocketTestCommand {
    val logger = LoggerFactory.getLogger(SocketTestCommand::class.java)
    val COMMAND_NAME = "sockettest"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Test socket echo")
                    .addOption(OptionType.STRING, "msg", "What to echo"), null
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
        val echoOpt = event.getOption("msg")?.asString ?: "test"
        if (!WebServer.hasSocketConnection()) {
            event.reply("No Socket Connection").setEphemeral(true).queue()
            return
        }
        var ping = System.currentTimeMillis()
        val result = ComputeJobManager.requestEcho(echoOpt).waitBlocking(5000)
        ping = System.currentTimeMillis() - ping
        if (result == null) {
            event.reply("Client failed to reply").setEphemeral(true).queue()
            return
        }
        val pingMsg = "Ping: $ping\nResult: ${result["result"]}"
        event.reply(pingMsg).setEphemeral(true).queue()
        logger.info("${event.member?.user?.name ?: "Unknown User"}: ${pingMsg}\n")
    }
}