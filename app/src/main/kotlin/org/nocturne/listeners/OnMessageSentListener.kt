package org.nocturne.listeners

import ch.qos.logback.classic.Logger
import net.dv8tion.jda.api.events.message.MessageReceivedEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter

class OnMessageSentListener : ListenerAdapter() {
    override fun onMessageReceived(event: MessageReceivedEvent) {
        if (event.author.isBot) return
        System.out.printf("[%s] %#s: %s\n",
            event.getChannel(),
            event.getAuthor(),
            event.getMessage().getContentDisplay());
    }
}