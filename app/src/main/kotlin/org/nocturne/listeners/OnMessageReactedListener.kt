package org.nocturne.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent

import java.awt.Color

object OnMessageReactedListener  {


    val REACTION_MILESTONE_CHANNEL = 1354697176554672188L
    val MILESTONE_EMOJI = Emoji.fromCustom("blue_fire",1352820065711554621,true)
    val EVENT_NAME = "MILESTONE_REACTION"
    var hasInit = false

    private fun registerToGlobalListeners() {
        GlobalListeners.onMessageReactionAddSubscribers[EVENT_NAME] = ::onMessageReactionAdd

    }
    fun init() {
        if (hasInit) return
        hasInit = true
        this.registerToGlobalListeners()
    }


    fun onMessageReactionAdd(event: MessageReactionAddEvent) {
        val retrieveMessage = event.retrieveMessage().complete().reactions
        var mostReacted = 0
        if (event.retrieveMessage().complete().author.isBot) return
        for(reaction in retrieveMessage) {
            if (mostReacted < reaction.count) {
                mostReacted = reaction.count
            }
        }


        val milestoneEmbed = EmbedBuilder()
            .setColor(Color(45,45,135))
            .setTitle("Reaction Milestone <:WHYY:1354110058136473632>")
            .setDescription("# ${event.retrieveMessage().complete().contentRaw} - ${event.retrieveMessage().complete().author.name}")
            .setUrl(event.jumpUrl)
            .build()

        if (mostReacted > 4) {
            event.guild.getTextChannelById(REACTION_MILESTONE_CHANNEL)?.sendMessage("<@${event.messageAuthorId}>")?.queue()

            event.guild.getTextChannelById(REACTION_MILESTONE_CHANNEL)?.sendMessageEmbeds(milestoneEmbed)?.queue()
        }


    }

}