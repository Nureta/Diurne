package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder
import org.apache.logging.log4j.util.StringMap
import org.nocturne.App
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.database.DataBaseManager
import org.nocturne.database.GuildAttributeManager
import org.nocturne.listeners.GlobalListeners
import org.nocturne.logic.AES
import org.slf4j.LoggerFactory
import java.awt.Color
import java.time.Duration
import java.util.*
import kotlin.collections.ArrayList

object HelperReportCommand {
    val logger = LoggerFactory.getLogger(HelperReportCommand::class.java)

    var REPORT_CHANNEL_ATTRIBUTE = "channel_report"
    var REPORT_ROLE_ATTRIBUTE = "role_helper_report"
    var REPORT_INFO_CHANNEL = "channel_report_info"

    val SETUP_HELPER_COMMAND_NAME = "setuphelper"
    val HELPER_DECRYPT_COMMAND_NAME = "decrypthelper"

    val REPORT_BUTTON_ID = "hr_report_button"

    val RESPONSE_YES_ID = "hr_response_yes"
    val RESPONSE_NEUTRAL_ID = "hr_response_neutral"
    val RESPONSE_NO_ID = "hr_response_no"
    val RESPONSE_DECRYPT_ID = "hr_response_decrypt"

    val REPORT_MODAL_ID = "hr_report_modal"
    val REPORT_MODAL_FIELD_DECISION = "hr_report_modal_decision"
    val REPORT_MODAL_FIELD_REASON = "hr_report_modal_reason"

    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        // Setup Helper Command
        CommandManager.updateCommandMap(
            MyCommand(
                SETUP_HELPER_COMMAND_NAME, Commands.slash(SETUP_HELPER_COMMAND_NAME, "Assign helper ticket to a channel").setDefaultPermissions(
                    DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)), null)
        )
        registerToGlobalListeners()
        GuildAttributeManager.addDefaultGuildAttribute(REPORT_CHANNEL_ATTRIBUTE, GuildAttributeManager.CHANNEL_TYPE)
        GuildAttributeManager.addDefaultGuildAttribute(REPORT_INFO_CHANNEL, GuildAttributeManager.CHANNEL_TYPE)
        GuildAttributeManager.addDefaultGuildAttribute(REPORT_ROLE_ATTRIBUTE, GuildAttributeManager.CHANNEL_TYPE)
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[SETUP_HELPER_COMMAND_NAME] = ::onSetupHelperCommand
        GlobalListeners.onButtonInteractionSubscribers[REPORT_BUTTON_ID] = ::onReportButtonInteraction
        GlobalListeners.onButtonInteractionSubscribers[RESPONSE_DECRYPT_ID] = ::onDecryptButtonInteraction

        GlobalListeners.onButtonInteractionSubscribers[RESPONSE_YES_ID] = ::onApproveButtonInteraction
        GlobalListeners.onButtonInteractionSubscribers[RESPONSE_NEUTRAL_ID] = ::onNeutralButtonInteraction
        GlobalListeners.onButtonInteractionSubscribers[RESPONSE_NO_ID] = ::onRejectButtonInteraction
        GlobalListeners.onModalInteractionSubscribers[REPORT_MODAL_ID] = ::onReportModalInteraction
    }

    private fun onSetupHelperCommand(event: SlashCommandInteractionEvent) {
        if (event.name.lowercase() != SETUP_HELPER_COMMAND_NAME) return
        val sender = event.member?.user?.id ?: return
        try {

            val helperReportInfoChannel = getReportInfoChannel(event.guild!!.idLong)
            if (helperReportInfoChannel == 0L) {
                event.reply("Please check helper report configuration").setEphemeral(true).queue()
                return
            }

            val helperEmbed = EmbedBuilder()
                .setTitle("Create a helper report")
                .setDescription("Provide attachments/proof, reason, and decision to made. \n IF none is provided logical reasoning is permitted/context")
                .setFooter("Vote for whether or not to invoke this executive decision")
                .setColor(Color(0x9efffd))
                .build()
            try {
                event.guild!!.getTextChannelById(helperReportInfoChannel)!!.sendMessageEmbeds(helperEmbed)
                    .addActionRow(Button.primary(REPORT_BUTTON_ID, "Submit a report!")).queue()
                event.reply("Message sent to assigned Channel!").setEphemeral(true).queue()
            } catch (e: Exception) {
                event.reply("An error occurred while trying to do Helper Report Setup.\n ${e.stackTrace}")
                    .setEphemeral(true).queue()
            }
        } catch (e: Exception) {
            logger.error(e.stackTraceToString())
            event.reply("Error\n```${e.stackTraceToString()}```").setEphemeral(true).queue()
        }
    }

    /**
     * If pressing on the "report" button, for creating a ticket, we'll open a modal for the user to submit info.
     */
    private fun onReportButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId.lowercase() != REPORT_BUTTON_ID) return
        val reportInputDecision = TextInput.create(REPORT_MODAL_FIELD_DECISION, "Decision", TextInputStyle.SHORT)
            .setPlaceholder("Report Title/Decisions you want made")
            .setMaxLength(60)
            .setMinLength(5)
            .build()
        val reportInputReason = TextInput.create(REPORT_MODAL_FIELD_REASON, "Reason", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Reason with evidence if available")
            .setMaxLength(200)
            .setMinLength(30)
            .build()
        val reportModal = Modal.create(REPORT_MODAL_ID, "Report")
            .addComponents(ActionRow.of(reportInputDecision), ActionRow.of(reportInputReason))
            .build()
        event.replyModal(reportModal).queue()
    }

    /**
     * After helper modal has been responded to, we'll create a thread and ping staff members.
     */
    private fun onReportModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId.lowercase() != REPORT_MODAL_ID) return
        helperModalAnonPoll(event)
    }

    private fun helperModalAnonPoll(event: ModalInteractionEvent) {
        val author = event.user.idLong
        val reportD = event.getValue(REPORT_MODAL_FIELD_DECISION)?.asString ?: return
        val reportR = event.getValue(REPORT_MODAL_FIELD_REASON)?.asString ?: return
        val reportEmbed = EmbedBuilder()
            .setTitle("Report Made!")
            .setDescription(reportD)
            .setFooter(reportR)
            .setColor(Color(0x74D7DFF))
            .build()

        try {
            event.deferReply().setEphemeral(true).queue()

            val reportChannel = getReportChannel(event.guild!!.idLong)
            val staffRolePing = getHelperReportRole(event.guild!!.idLong)
            if (reportChannel == 0L || staffRolePing == 0L) {
                event.reply("Please do /adminconfigure and check helper report configuration")
                return
            }

            val resultData = ReportResultData()
            resultData.author = "${event.user.name} ($author)"


            val sentReport = event.guild!!.getTextChannelById(reportChannel)!!.sendMessageEmbeds(reportEmbed).complete()
            val thread = sentReport.createThreadChannel(reportD).complete()
            val actionMsg = thread.sendMessage("# $reportR  \n### $reportD \n \n *(Provide any Images/Proofs)* \n-# <@&$staffRolePing>")
                .addActionRow(Button.success(RESPONSE_YES_ID, "Approve"),
                    Button.secondary(RESPONSE_NEUTRAL_ID, "Neutral"),
                    Button.danger(RESPONSE_NO_ID, "Reject"))
                .addActionRow(Button.secondary(RESPONSE_DECRYPT_ID, "Decrypt"))
                .complete()


            val dataEmbed = getVoteResultEmbed(encryptResultData(resultData, actionMsg.idLong.toString()))
            actionMsg.editMessageEmbeds(dataEmbed).queue()

            event.hook.sendMessage("Report Submitted.").setEphemeral(true).queue()
        } catch (e: Exception) {
            event.hook.sendMessage("Error, failed to create report").setEphemeral(true).queue()
        }

    }

    private fun helperModalPublicPoll(event: ModalInteractionEvent) {
        val reportD = event.getValue(REPORT_MODAL_FIELD_DECISION)?.asString ?: return
        val reportR = event.getValue(REPORT_MODAL_FIELD_REASON)?.asString ?: return

        val reportEmbed = EmbedBuilder()
            .setTitle("Report made by ${event.member?.user?.name ?: "unknown"}")
            .setDescription(reportD)
            .setFooter(reportR)
            .setColor(Color(0x74D7DFF))
            .build()
        val votePoll = MessagePollBuilder("Vote for $reportR")
            .addAnswer("Yes")
            .addAnswer("Neutral")
            .addAnswer("No")
            .setDuration(Duration.ofHours(24L))
            .build()


        try {
            val reportChannel = getReportChannel(event.guild!!.idLong)
            val staffRolePing = getHelperReportRole(event.guild!!.idLong)
            if (reportChannel == 0L || staffRolePing == 0L) {
                event.reply("Please do /adminconfigure and check helper report configuration")
                return
            }

            val sentReport = event.guild!!.getTextChannelById(reportChannel)!!.sendMessageEmbeds(reportEmbed).complete()
            val thread = sentReport.createThreadChannel(reportD).complete()
            thread.sendMessage("# $reportR  \n### $reportD \n \n *(Provide any Images/Proofs)* \n-# <@&$staffRolePing>")
                .complete()
            thread.sendMessagePoll(votePoll).queue { sentPoll ->
                sentPoll.pin().queue()
            }
            event.reply("Report has been sent!").setEphemeral(true).queue()

        } catch (e: Exception) {
            event.reply("Error, failed to create report.\n```${e.stackTraceToString()}```").setEphemeral(true).queue()
        }
    }



    // region Vote BUTTON Responses


    private fun onApproveButtonInteraction(event: ButtonInteractionEvent) {
        val embed = event.message.embeds[0]
        val resultData = parseVoteResultEmbed(embed)
        val name = event.user.name
        val userid = event.user.idLong
        val msgId = event.messageIdLong.toString()
        val salt = Base64.getEncoder().encodeToString(msgId.toByteArray())

        val userData = AES.encrypt(AES.encrypt("$name `$userid`", App.helperReportKey), salt)

        if (resultData.accept.contains(userData)) {
            event.reply("You've already voted for approve.").setEphemeral(true).queue()
            return
        }
        resultData.reject.remove(userData)
        resultData.neutral.remove(userData)
        resultData.accept.add(userData)

        val updatedEmbed = getVoteResultEmbed(resultData)
        event.message.editMessageEmbeds(updatedEmbed).queue()
        event.reply("Approve Vote Registered.").setEphemeral(true).queue()
    }

    private fun onNeutralButtonInteraction(event: ButtonInteractionEvent) {
        val embed = event.message.embeds[0]
        val resultData = parseVoteResultEmbed(embed)
        val name = event.user.name
        val userid = event.user.idLong
        val msgId = event.messageIdLong.toString()
        val salt = Base64.getEncoder().encodeToString(msgId.toByteArray())

        val userData = AES.encrypt(AES.encrypt("$name `$userid`", App.helperReportKey), salt)
        if (resultData.neutral.contains(userData)) {
            event.reply("You've already voted for neutral.").setEphemeral(true).queue()
            return
        }
        resultData.accept.remove(userData)
        resultData.reject.remove(userData)
        resultData.neutral.add(userData)

        val updatedEmbed = getVoteResultEmbed(resultData)
        event.message.editMessageEmbeds(updatedEmbed).queue()
        event.reply("Neutral Vote Registered.").setEphemeral(true).queue()
    }

    private fun onRejectButtonInteraction(event: ButtonInteractionEvent) {
        val embed = event.message.embeds[0]
        val resultData = parseVoteResultEmbed(embed)
        val name = event.user.name
        val userid = event.user.idLong
        val msgId = event.messageIdLong.toString()
        val salt = Base64.getEncoder().encodeToString(msgId.toByteArray())



        val userData = AES.encrypt(AES.encrypt("$name `$userid`", App.helperReportKey), salt)
        if (resultData.reject.contains(userData)) {
            event.reply("You've already voted for reject.").setEphemeral(true).queue()
            return
        }
        resultData.accept.remove(userData)
        resultData.neutral.remove(userData)
        resultData.reject.add(userData)

        val updatedEmbed = getVoteResultEmbed(resultData)
        event.message.editMessageEmbeds(updatedEmbed).queue()
        event.reply("Reject Vote Registered.").setEphemeral(true).queue()
    }

    private fun onDecryptButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId.lowercase() != RESPONSE_DECRYPT_ID) return
        val sender = event.member?.user?.id ?: return
        if (!adminUsers.contains(sender)) {
                event.reply("Only Meet/Nureta may decrypt").setEphemeral(true).queue()
            return
        }

        val msg = event.message
        val embed = msg.embeds[0]

        val encryptedData = parseVoteResultEmbed(embed)
        val resultData = decryptResultData(encryptedData, event.messageIdLong.toString())

        val decryptedEmbed = getVoteResultEmbed(resultData)

        event.reply("Decrypted!").addEmbeds(decryptedEmbed).setEphemeral(true).queue()
    }

    private fun parseVoteResultEmbed(embed: MessageEmbed): EncryptedResultData {
        val result = EncryptedResultData()
        val creatorEnc = embed.footer!!.text!!.removePrefix("Created By:").trim()

        val desc = embed.description ?: ""
        val split = desc.split("---")
        val accepted = split[0]
        val acceptedLines = accepted.lines()

        for (line in acceptedLines) {
            if (line.lowercase().contains("approve")) continue
            val cleaned = line.removeSurrounding("`").trim()
            if (cleaned.isEmpty()) continue
            result.accept.add(cleaned)
        }

        val neutral = split[1]
        val neutralLines = neutral.lines()
        for (line in neutralLines) {
            if (line.lowercase().contains("neutral")) continue
            val cleaned = line.removeSurrounding("`").trim()
            if (cleaned.isEmpty()) continue
            result.neutral.add(cleaned)
        }

        val reject = split[2]
        val rejectLines = reject.lines()
        for (line in rejectLines) {
            if (line.lowercase().contains("reject")) continue
            val cleaned = line.removeSurrounding("`").trim()
            if (cleaned.isEmpty()) continue
            result.reject.add(cleaned)
        }

        result.author = creatorEnc
        return result
    }

    private fun getVoteResultEmbed(accept: List<String>, neutral: List<String>,
                                   reject: List<String>, author: String,
                                   escape: Boolean = true): MessageEmbed {
        var desc = StringBuilder("Approve\n")
        for (a in accept) {
            if (escape)
                desc.append("`${a}`\n")
            else
                desc.append("${a}\n")
        }
        desc.append("---\n")

        desc.append("Neutral\n")
        for (n in neutral) {
            if (escape)
                desc.append("`${n}`\n")
            else
                desc.append("${n}\n")
        }
        desc.append("---\n")

        desc.append("Reject\n")
        for (r in reject) {
            if (escape)
                desc.append("`${r}`\n")
            else
                desc.append("${r}\n")
        }
        desc.append("---\n")

        val dataEmbed = EmbedBuilder()
            .setTitle("Approve: ${accept.size}, Neutral: ${neutral.size}, Reject: ${reject.size}")
            .setDescription(desc)
            .setFooter("Created By: $author")
            .build()
        return dataEmbed
    }
    private fun getVoteResultEmbed(resultData: EncryptedResultData): MessageEmbed {
        val accept = resultData.accept
        val neutral = resultData.neutral
        val reject = resultData.reject
        val author = resultData.author
        return getVoteResultEmbed(accept.toList(), neutral.toList(), reject.toList(), author)
    }
    private fun getVoteResultEmbed(resultData: ReportResultData): MessageEmbed {
        val accept = resultData.accept
        val neutral = resultData.neutral
        val reject = resultData.reject
        val author = resultData.author
        return getVoteResultEmbed(accept, neutral, reject, author, false)
    }

    private fun decryptResultData(data: EncryptedResultData, msgId: String): ReportResultData {
        val salt = Base64.getEncoder().encodeToString(msgId.toByteArray())
        val result = ReportResultData()
        for (d in data.accept) {
            val dec = AES.decrypt(AES.decrypt(d, salt), App.helperReportKey)
            result.accept.add(dec)
        }
        for (d in data.neutral) {

            val dec = AES.decrypt(AES.decrypt(d, salt), App.helperReportKey)
            result.neutral.add(dec)
        }
        for (d in data.reject) {
            val unsalted = AES.decrypt(d, salt)
            val dec = AES.decrypt(unsalted, App.helperReportKey)
            result.reject.add(dec)
        }
        result.author = AES.decrypt(AES.decrypt(data.author, salt), App.helperReportKey)
        return result
    }

    private fun encryptResultData(data: ReportResultData, msgId: String): EncryptedResultData {
        val salt = Base64.getEncoder().encodeToString(msgId.toByteArray())
        var result = EncryptedResultData()
        for (d in data.accept) {
            val enc = AES.encrypt(d, App.helperReportKey)
            result.accept.add(AES.encrypt(enc, salt))
        }
        for (d in data.neutral) {
            val enc = AES.encrypt(d, App.helperReportKey)
            result.neutral.add(AES.encrypt(enc, salt))
        }
        for (d in data.reject) {
            val enc = AES.encrypt(d, App.helperReportKey)
            result.reject.add(AES.encrypt(enc, salt))
        }

        val encAuthor = AES.encrypt(data.author, App.helperReportKey)
        result.author = AES.encrypt(encAuthor, salt)
        return result
    }


    private fun cleanResultData(data: EncryptedResultData) {
    }

    // endregion

    private fun getReportChannel(guildId: Long): Long {
        val reportChannel = DataBaseManager.genericAttributes
            .getAttribute(REPORT_CHANNEL_ATTRIBUTE, guildId).executeAsOneOrNull()
        if (reportChannel == null || reportChannel.data_.isNullOrEmpty()) {
            return 0
        }
        return reportChannel.data_.toLongOrNull() ?: 0
    }

    private fun getHelperReportRole(guildId: Long): Long {
        val helperRole = DataBaseManager.genericAttributes
            .getAttribute(REPORT_ROLE_ATTRIBUTE, guildId).executeAsOneOrNull()
        if (helperRole == null || helperRole.data_.isNullOrEmpty()) {
            return 0
        }
        return helperRole.data_.toLongOrNull() ?: 0
    }

    private fun getReportInfoChannel(guildId: Long): Long {
        val reportInfoChannel = DataBaseManager.genericAttributes
            .getAttribute(REPORT_INFO_CHANNEL, guildId).executeAsOneOrNull()
        if (reportInfoChannel == null || reportInfoChannel.data_.isNullOrEmpty()) {
            return 0
        }
        return reportInfoChannel.data_.toLongOrNull() ?: 0
    }


    private class EncryptedResultData {
        var accept: MutableSet<String> = mutableSetOf()
        var neutral: MutableSet<String> = mutableSetOf()
        var reject: MutableSet<String> = mutableSetOf()
        var author: String = ""
    }

    private class ReportResultData {
        var accept: ArrayList<String> = ArrayList()
        var neutral: ArrayList<String> = ArrayList()
        var reject: ArrayList<String> = ArrayList()
        var author: String = ""
    }
}
