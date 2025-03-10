package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder
import org.nocturne.listeners.GlobalListeners
import java.awt.Color
import java.time.Duration

object HelperReportCommand {
    var reportChannel = 1347776201615474688L
    var staffRolePing = 1347387864165384243L
    var helperReportInfoChannel = 1347819027858194473L

    val COMMAND_NAME = "setuphelper"

    val REPORT_BUTTON_ID = "hr_report_button"

    val REPORT_MODAL_ID = "hr_report_modal"
    val REPORT_MODAL_FIELD_DECISION = "hr_report_modal_decision"
    val REPORT_MODAL_FIELD_REASON = "hr_report_modal_reason"

    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Assign helper ticket to a channel"), null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
        GlobalListeners.onButtonInteractionSubscribers[REPORT_BUTTON_ID] = ::onReportButtonInteraction
        GlobalListeners.onModalInteractionSubscribers[REPORT_MODAL_ID] = ::onReportModalInteraction
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        if (event.name.lowercase() != COMMAND_NAME) return
        val sender = event.member?.user?.id ?: return
        if (!CommandManager.adminUsers.contains(sender)) {
            event.reply("You do not have permissions to run this command!").setEphemeral(true).queue()
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
            .setPlaceholder("Reason with evidence if avaliable")
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
            .addAnswer("No")
            .setDuration(Duration.ofMinutes(60L))
            .build()

        try {
            val sentReport = event.guild!!.getTextChannelById(reportChannel)!!.sendMessageEmbeds(reportEmbed).complete()
            val thread = sentReport.createThreadChannel(reportD).complete()
            thread.sendMessage("# $reportR  \n### $reportD \n \n *(Provide any Images/Proofs)* \n-# <@&$staffRolePing>")
                .complete()
            thread.sendMessagePoll(votePoll).queue { sentPoll ->
                sentPoll.pin().queue()
            }
            event.reply("Report has been sent!").setEphemeral(true).queue()

        } catch (e: Exception) {
            event.reply("Error, failed to create report.").setEphemeral(true).queue()
        }
    }


}