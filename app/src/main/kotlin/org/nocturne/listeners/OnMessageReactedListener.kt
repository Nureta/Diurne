package org.nocturne.listeners

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.Message.Attachment
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent
import org.nocturne.database.DataBaseManager.REACT_MILESTONE

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
        val retrievedMessage = event.retrieveMessage().complete()
        var mostReacted = 0
        if (event.retrieveMessage().complete().author.isBot) return


        for(reaction in retrievedMessage.reactions) {
            if (mostReacted < reaction.count) {
                mostReacted = reaction.count
            }
        }


        val milestoneEmbed = EmbedBuilder()
            .setColor(Color(45,45,135))
            .setTitle("Click to view <:WHYY:1354110058136473632>")
            .setAuthor("Reaction Milestone ")
            .setThumbnail(retrievedMessage.author.effectiveAvatarUrl)
            .setDescription("## ${retrievedMessage.contentRaw}")
            .setFooter("—${retrievedMessage.author.name}")
            .setUrl(event.jumpUrl)
            .build()

        if (mostReacted > 4) {
            if (REACT_MILESTONE.selectMessageByMessageId(retrievedMessage.idLong).executeAsOneOrNull() == null) {
                REACT_MILESTONE.insertMessage(retrievedMessage.idLong)
                event.guild.getTextChannelById(REACTION_MILESTONE_CHANNEL)
                    ?.sendMessage("<@${event.messageAuthorId}>")
                    ?.addEmbeds(milestoneEmbed)
                    ?.queue()
            }
        }


    }

}