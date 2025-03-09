package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import net.dv8tion.jda.api.interactions.modals.Modal
import javax.swing.Action

class SetupHelperCommand : ListenerAdapter() {
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        when (event.componentId) {
            "report" -> {
                val reportInputDecision = TextInput.create("reportD","Decision", TextInputStyle.SHORT)
                    .setPlaceholder("Report Title/Decisions you want made")
                    .setMaxLength(60)
                    .setMinLength(5)
                    .build()
                val reportInputReason = TextInput.create("reportR","Reason", TextInputStyle.PARAGRAPH)
                    .setPlaceholder("Reason with evidence if avaliable")
                    .setMaxLength(200)
                    .setMinLength(30)
                    .build()
                val reportModal = Modal.create("report","Report")
                    .addComponents(ActionRow.of(reportInputDecision),ActionRow.of(reportInputReason))
                    .build()
                event.replyModal(reportModal).queue()

            }


        }
    }
}