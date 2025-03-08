package org.nocturne.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.utils.messages.MessagePollData
import java.awt.Color
import java.time.Duration
import java.util.*
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object ModalListener : ListenerAdapter() {
    init {
        val props = Properties()
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment")
    }

    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId == "confession") {
            var confession = event.getValue("confession")?.asString ?: return

            var confessionEmbed = EmbedBuilder()
                .setTitle("Confession")
                .setDescription(confession)
                .build()

            event.guild!!.getTextChannelById(1326855844561682452)!!.sendMessageEmbeds(confessionEmbed)
                .setActionRow(
                    Button.primary("newConfession", "Submit a new confession!")
                ).queue()
            event.reply("Confession has been sent!").setEphemeral(true).queue()
        }
        if (event.modalId == "report") {
            var report = event.getValue("report")?.asString ?: return

            val reportEmbed = EmbedBuilder()
                .setTitle("Report made by ${event.member?.user?.name ?: "unknown"}")
                .setDescription(report)
                .setColor(Color(0x74D7DFF))
                .build()
            var data = MessagePollData.builder("Invoke Decision?")
                .addAnswer("Yes")
                .addAnswer("No")
                .setDuration(Duration.ofMinutes(30))
                .build()
            val sentReport = event.guild!!.getTextChannelById(1347776201615474688)!!.sendMessageEmbeds(reportEmbed).complete()
            val thread = sentReport.createThreadChannel("report").complete()
            thread.sendMessage("# **PLEASE TELL ME THINGS** (Images/Proofs) \n <@&1347387864165384243>").queue()
            thread.sendMessagePoll(data).queue()
            event.reply("Report has been sent!").setEphemeral(true).queue()
        }
    }

    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.componentId) {
            "newConfession" -> {
                val confessionInput = TextInput.create("confession", "confession", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Confession")
                    .setMaxLength(400)
                    .setMinLength(10)
                    .build()
                val confessionModal = Modal.create("confession", "Confession")
                    .addComponents(ActionRow.of(confessionInput))
                    .build()
                event.replyModal(confessionModal).queue() // send a message in the channel
            }


        }
    }
}

