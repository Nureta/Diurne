package org.nocturne.listeners
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.internal.utils.JDALogger

class OnMessageSentListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        println(
            "[${event.getChannel()}] ${event.author}: ${event.message.contentDisplay}\n",
        )
    }
}