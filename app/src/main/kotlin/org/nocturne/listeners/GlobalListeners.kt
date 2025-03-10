package org.nocturne.listeners

import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.nocturne.commands.CommandManager

object GlobalListeners : ListenerAdapter() {
    val onReadySubscribers = HashMap<String, ((ReadyEvent) -> Unit)>()
    override fun onReady(event: ReadyEvent) {
        super.onReady(event)
        for (subscriberCallback in onReadySubscribers.values) {
            subscriberCallback(event)
        }
        CommandManager.initializeCommands(event.jda)
    }

    val onMessageReceivedSubscribers = HashMap<String, ((MessageReceivedEvent) -> Unit)>()
    override fun onMessageReceived(event: MessageReceivedEvent) {
        super.onMessageReceived(event)
        for (subscriberCb in onMessageReceivedSubscribers.values) {
            subscriberCb(event)
        }
    }

    val onModalInteractionSubscribers = HashMap<String, ((ModalInteractionEvent) -> Unit)>()
    override fun onModalInteraction(event: ModalInteractionEvent) {
        super.onModalInteraction(event)
        val cb = onModalInteractionSubscribers[event.modalId]
        cb?.invoke(event)
    }

    val onButtonInteractionSubscribers = HashMap<String, ((ButtonInteractionEvent) -> Unit)>()
    override fun onButtonInteraction(event: ButtonInteractionEvent) {
        super.onButtonInteraction(event)
        val cb = onButtonInteractionSubscribers[event.componentId]
        cb?.invoke(event)
    }

    val onSlashCommandInteractionSubscribers = HashMap<String, ((SlashCommandInteractionEvent) -> Unit)>()
    override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
        super.onSlashCommandInteraction(event)
        val cb = onSlashCommandInteractionSubscribers[event.name.lowercase()]
        cb?.invoke(event)
    }

}

