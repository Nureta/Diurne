package org.nocturne.listeners

import net.dv8tion.jda.api.events.guild.GuildJoinEvent
import net.dv8tion.jda.api.events.guild.GuildReadyEvent
import net.dv8tion.jda.api.events.guild.member.GuildMemberJoinEvent
import net.dv8tion.jda.api.events.guild.voice.GuildVoiceUpdateEvent
import net.dv8tion.jda.api.events.interaction.ModalInteractionEvent
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.nocturne.commands.CommandManager
import org.nocturne.database.GuildAttributeManager
import org.nocturne.services.LevelingService

object GlobalListeners : ListenerAdapter() {

    val onReadySubscribers = HashMap<String, ((ReadyEvent) -> Unit)>()
    override fun onReady(event: ReadyEvent) {
        super.onReady(event)
        for (subscriberCallback in onReadySubscribers.values) {
            subscriberCallback(event)
        }
        CommandManager.initializeCommands(event.jda)
        CommandManager.registerAllCommandMapCommands()
    }

    val onGuildReadyEventSubscribers = HashMap<String, ((GuildReadyEvent) -> Unit)>()
    override fun onGuildReady(event: GuildReadyEvent) {
        super.onGuildReady(event)
        for (subscriberCallback in onGuildReadyEventSubscribers.values) {
            subscriberCallback(event)
        }
        CommandManager.initializeCommands(event.jda)
        CommandManager.registerAllCommandMapCommands()
        GuildAttributeManager.initGuild(event.guild.idLong)
    }

    val onGuildMemberJoinEventSubscribers = HashMap<String, ((GuildMemberJoinEvent) -> Unit)>()
    override fun onGuildMemberJoin(event: GuildMemberJoinEvent) {
        super.onGuildMemberJoin(event)
        for (subscriberCallback in onGuildMemberJoinEventSubscribers.values) {
            subscriberCallback(event)
        }
    }

    val onMessageReplySubscribers = HashMap<String, ((MessageReceivedEvent) -> Unit)>()
    fun onMessageReply(event: MessageReceivedEvent) {
        for (subscriberCb in onMessageReplySubscribers.values) {
            subscriberCb(event)
        }
    }

    val onMessageReceivedSubscribers = HashMap<String, ((MessageReceivedEvent) -> Unit)>()
    override fun onMessageReceived(event: MessageReceivedEvent) {
        super.onMessageReceived(event)
        if (event.message.messageReference != null) {
            this.onMessageReply(event)
        }
        for (subscriberCb in onMessageReceivedSubscribers.values) {
            subscriberCb(event)
        }
    }

    val onMessageReactionAddSubscribers = HashMap<String, ((MessageReactionAddEvent) -> Unit)>()
    override fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        super.onMessageReactionAdd(event)
        for (subscriberCb in onMessageReactionAddSubscribers.values) {
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
    init{
        OnMessageReactedListener.init()
        LevelingService.init()
    }
}

