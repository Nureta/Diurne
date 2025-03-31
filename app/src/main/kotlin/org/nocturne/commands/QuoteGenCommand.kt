package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.utils.FileUpload
import org.nocturne.listeners.GlobalListeners
import org.nocturne.webserver.ComputeJobManager
import org.nocturne.webserver.WebServer
import org.slf4j.LoggerFactory
import java.awt.Color
import java.io.File


object QuoteGenCommand {
    val logger = LoggerFactory.getLogger(QuoteGenCommand::class.java)
    val COMMAND_NAME = "quote"
    private var hasInit = false

    val disconnectedEmoji = Emoji.fromUnicode("U+1F4F4")
    val cancelEmoji = Emoji.fromUnicode("U+274C")
    val hourglassEmoji = Emoji.fromUnicode("U+23F3")

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Quote a user!")
                    .addOption(OptionType.STRING, "quote", "Quote", true)
                    .addOption(OptionType.USER, "user", "User", true), null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
        GlobalListeners.onMessageReplySubscribers[COMMAND_NAME] = ::onMessageReply
    }

    private fun onMessageReply(event: MessageReceivedEvent) {
        val msg = event.message.contentDisplay

        if (!msg.startsWith("!quote")) return

        val replyMsg = event.message.referencedMessage ?: return
        val rmsg = replyMsg.contentDisplay
        var rAuthor = replyMsg.member
        if (rAuthor == null) {
            rAuthor = event.guild.retrieveMemberById(replyMsg.author.idLong).complete()
        }
        if (!WebServer.hasSocketConnection()) {
            event.message.addReaction(disconnectedEmoji).queue()
            return
        }
        if (rAuthor == null) {
            event.message.addReaction(cancelEmoji).queue()
            return
        }

        event.message.addReaction(hourglassEmoji).queue()
        ComputeJobManager.generateQuoteAI(rmsg, rAuthor.nickname ?: rAuthor.effectiveName).waitCallback(30000) { result ->
            if (result == null) {
                event.message.removeReaction(hourglassEmoji).queue()
                event.message.addReaction(cancelEmoji).queue()
                return@waitCallback
            }
            val file = File(result["filepath"]!!)
            if (file.exists()) {
                val embed = EmbedBuilder()
                embed.setAuthor(rAuthor.user.name, null, rAuthor.effectiveAvatarUrl)
                embed.setColor(Color.blue)
                embed.setImage("attachment://quote.png")
                event.message.replyEmbeds(embed.build())
                    .addContent(replyMsg.jumpUrl)
                    .addFiles(FileUpload.fromData(file, "quote.png")).queue()
            }
        }
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val quoteMsg = event.getOption("quote")?.asString ?: return
        val authOpt = event.getOption("user") ?: return
        val authUser = authOpt.asUser
        var authMember = authOpt.asMember
        if (authMember == null) {
             authMember = event.guild?.retrieveMemberById(authUser.idLong)?.complete()
        }
        if (authMember == null) return
        val author = authMember.nickname ?: authUser.name

        if (!WebServer.hasSocketConnection()) {
            event.reply("No connection").setEphemeral(true).queue()
            return
        }
        event.deferReply(true).queue()
        var ping = System.currentTimeMillis()
        val result = ComputeJobManager.generateQuoteAI(quoteMsg, author).waitBlocking(30000)
        ping = System.currentTimeMillis() - ping
        if (result == null) {
            event.hook.sendMessage("Client failed to reply").setEphemeral(true).queue()
            return
        }
        val pingMsg = "Ping: $ping\nResult: ${result["filename"]}"
        event.hook.sendMessage(pingMsg).setEphemeral(true).queue()

        val file = File(result["filepath"]!!)
        if (file.exists()) {
            val embed = EmbedBuilder()
            embed.setImage("attachment://quote.png")
            embed.setAuthor(authUser.name, null, authUser.effectiveAvatarUrl)
            embed.setFooter("⚠ Quote may not be accurate or truthful.")
            event.channel.sendMessageEmbeds(embed.build())
                .setContent(authUser.asMention)
                .addFiles(FileUpload.fromData(file, "quote.png")).queue()
        }
        logger.info("${event.member?.user?.name ?: "Unknown User"}: ${pingMsg}\n")
    }
}