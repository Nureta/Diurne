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
import net.dv8tion.jda.api.utils.messages.MessagePollBuilder
import net.dv8tion.jda.api.utils.messages.MessagePollData
import java.awt.Color
import java.time.Duration
import java.time.Duration.ofMinutes
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.reflect.typeOf
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
            var reportD = event.getValue("reportD")?.asString ?: return
            var reportR = event.getValue("reportR")?.asString ?: return

            val reportEmbed = EmbedBuilder()
                .setTitle("Report made by ${event.member?.user?.name ?: "unknown"}")
                .setDescription(reportD)
                .setFooter(reportR)
                .setColor(Color(0x74D7DFF))
                .build()
            val votePoll = MessagePollBuilder("Vote for $reportR")
                .addAnswer("Yes")
                .addAnswer("No")
                .setDuration(1800L, TimeUnit.SECONDS)
                .build()


            val sentReport = event.guild!!.getTextChannelById(1347776201615474688)!!.sendMessageEmbeds(reportEmbed).complete()
            val thread = sentReport.createThreadChannel(reportD).complete()
            val message = thread.sendMessage("# $reportR  \n### $reportD \n \n *(Provide any Images/Proofs)* \n-# <@&1347387864165384243>").complete()
            thread.sendMessagePoll(votePoll)


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

