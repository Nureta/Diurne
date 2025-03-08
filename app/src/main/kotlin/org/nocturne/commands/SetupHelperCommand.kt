package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal

class SetupHelperCommand : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.componentId) {
            "report" -> {
                val reportInput = TextInput.create("report","report", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Report Title, \nExclaim reasons and decisions you want made!")
                    .setMaxLength(120)
                    .setMinLength(30)
                    .build()
                val reportModal = Modal.create("report","Report")
                    .addComponents(ActionRow.of(reportInput))
                    .build()
                event.replyModal(reportModal).queue()

            }


        }
    }
}