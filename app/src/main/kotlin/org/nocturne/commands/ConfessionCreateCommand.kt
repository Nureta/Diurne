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
import org.nocturne.listeners.GlobalListeners
import org.nocturne.webserver.ComputeJobManager
import org.nocturne.webserver.WebServer

object ConfessionCreateCommand {
    val COMMAND_NAME = "confession"
    private var hasInit = false

    val CONFESSION_MODAL_ID = "confession_modal"
    val CONFESSION_MODAL_TEXT_ID = "confession_modal_text"
    val CONFESSION_BUTTON_NEW = "confession_btn_new"

    var confessionChannel = 1326855844561682452L

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(COMMAND_NAME, Commands.slash(COMMAND_NAME,"Send a anonymous confession"), null)
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
        GlobalListeners.onModalInteractionSubscribers[CONFESSION_MODAL_ID] = ::onConfessionModalInteraction
        GlobalListeners.onButtonInteractionSubscribers[CONFESSION_BUTTON_NEW] = ::onNewConfessionButtonInteraction
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        event.replyModal(getConfessionModal()).queue()
    }

    /**
     * send confession message in appropriate chanel after modal is done being responded to.
     */
    private fun onConfessionModalInteraction(event: ModalInteractionEvent) {
        var confession = event.getValue(CONFESSION_MODAL_TEXT_ID)?.asString ?: return



        // event.reply("Confession processed!").setEphemeral(true).queue()

        // Try getting a toxicity reading
        val toxic = checkToxicity(confession)
        if (!toxic.isNullOrEmpty()) {
            confession += "\n${toxic}"
        }
        val confessionEmbed = EmbedBuilder()
            .setTitle("Confession")
            .setDescription(confession)
            .build()

        event.guild!!.getTextChannelById(confessionChannel)!!.sendMessageEmbeds(confessionEmbed)
            .setActionRow(
                Button.primary(CONFESSION_BUTTON_NEW, "Submit a new confession!")
            ).queue()
        event.reply("Confession has been sent!").setEphemeral(true).queue()
    }


    private fun checkToxicity(confession: String): String? {
        if (!WebServer.hasSocketConnection()) return null

        val toxicity = ComputeJobManager.requestToxicCheck(confession).waitBlocking(5000)
        if (toxicity.isNullOrEmpty()) return null
        val neutral = toxicity["neutral"]
        val toxic = toxicity["toxic"]
        return "Toxicity: $toxic"
    }

    /**
     * When user presses "new confession" show modal
     */
    private fun onNewConfessionButtonInteraction(event: ButtonInteractionEvent) {
        if (event.componentId != CONFESSION_BUTTON_NEW) return
        event.replyModal(getConfessionModal()).queue() // send a message in the channel
    }

    /**
     * Return modal that is shown to users when making confessions
     */
    private fun getConfessionModal(): Modal {
        val confessionInput = TextInput.create(CONFESSION_MODAL_TEXT_ID, "confession", TextInputStyle.PARAGRAPH)
            .setPlaceholder("Confession")
            .setMaxLength(400)
            .setMinLength(10)
            .build()
        val confessionModal = Modal.create(CONFESSION_MODAL_ID, "Confession")
            .addComponents(ActionRow.of(confessionInput))
            .build()
        return confessionModal
    }
}