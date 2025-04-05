package org.nocturne.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import okhttp3.internal.concurrent.formatDuration
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.listeners.GlobalListeners
import kotlin.concurrent.timer
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.toDuration

object ChatReviveCommand {
    val COMMAND_NAME = "chatrevive"
    var lastUsed = System.currentTimeMillis()
    val PING_COOLDOWN = (60).toDuration(DurationUnit.MINUTES).inWholeMilliseconds
    val CHAT_REVIVE_ROLE = "1353610494241673266"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Request people to talk!")
                    .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                 , null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val currentTime = System.currentTimeMillis()
        if ( PING_COOLDOWN < (currentTime - lastUsed) ) {
            event.reply("HEY! <@&$CHAT_REVIVE_ROLE> Someone wants to talk! <a:AngelBunny:1352820070560038994>").setEphemeral(false).queue { msg ->
                lastUsed = currentTime
                event.reply("Success").setEphemeral(true).queue()
            }
        } else {
            val cooldownTime = (PING_COOLDOWN - (currentTime - lastUsed)).toDuration(DurationUnit.MILLISECONDS).inWholeMinutes
            event.reply("Please wait, **$cooldownTime** minutes!").setEphemeral(true).queue()
        }
    }
}