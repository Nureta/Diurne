package org.nocturne.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle


object ModalListener : ListenerAdapter() {
    override fun onModalInteraction(event: ModalInteractionEvent) {
        if (event.modalId == "confession") {
            val confession = event.getValue("confession") ?: return
            var confessionEmbed = EmbedBuilder()
                .setTitle("Confession")
                .setDescription(confession.asString)
                .build()

            event.guild!!.getTextChannelById(1326855844561682452)!!.sendMessageEmbeds(confessionEmbed)
                .setActionRow(
                    Button.primary("newConfession","Submit a new confession!")
                ).queue()


            event.reply("Confession has been sent!").setEphemeral(true).queue()
        }
    }
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.componentId) {
            "newConfession" -> {
                val confessionInput = TextInput.create("confession","confession", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Confession")
                .setMaxLength(400)
                .setMinLength(10)
                .build()
                val confessionModal = Modal.create("confession","Confession")
                .addComponents(ActionRow.of(confessionInput))
                .build()
                event.replyModal(confessionModal).queue() // send a message in the channel
            }


        }
    }
}

