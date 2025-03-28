package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.listeners.GlobalListeners
import org.nocturne.sockets.SocketManager
import org.slf4j.LoggerFactory
import java.io.File


object QuoteGenCommand {
    val logger = LoggerFactory.getLogger(QuoteGenCommand::class.java)
    val COMMAND_NAME = "quote"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Quote a user!")
                    .addOption(OptionType.STRING, "quote", "Quote")
                    .addOption(OptionType.USER, "user", "User"), null
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
        val quoteMsg = event.getOption("quote")?.asString ?: return
        val author = event.getOption("user")?.asUser?.name ?: return
        val conn = SocketManager.clientConnection
        if (conn == null) {
            event.reply("No connection").setEphemeral(true).queue()
            return
        }
        event.deferReply(true).queue()
        var ping = System.currentTimeMillis()
        val result = conn.requestQuoteGen(quoteMsg, author).waitBlocking(30000)
        ping = System.currentTimeMillis() - ping
        if (result == null) {
            event.hook.sendMessage("Client failed to reply").setEphemeral(true).queue()
            return
        }
        val pingMsg = "Ping: $ping\nResult: $result"
        event.hook.sendMessage(pingMsg).setEphemeral(true).queue()

        if (result == "FAILURE") return
        val file = File(result)
        if (file.exists()) {
            val embed = EmbedBuilder()
            embed.setImage("attachment://quote.png")
            event.channel.sendMessageEmbeds(embed.build())
                .addFiles(FileUpload.fromData(file, "quote.png")).queue()
        }
        logger.info("${event.member?.user?.name ?: "Unknown User"}: ${pingMsg}\n")
    }
}