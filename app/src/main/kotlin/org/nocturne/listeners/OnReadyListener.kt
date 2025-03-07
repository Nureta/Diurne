package org.nocturne.listeners

import net.dv8tion.jda.api.events.session.ReadyEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import org.nocturne.commands.CommandManager

class OnReadyListener : ListenerAdapter() {
    override fun onReady(event: ReadyEvent) {
        super.onReady(event)
        CommandManager.InitializeCommands(event.jda)

    }
}