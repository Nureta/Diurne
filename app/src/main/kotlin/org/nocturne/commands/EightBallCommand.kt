package org.nocturne.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.listeners.GlobalListeners
import kotlin.random.Random

object EightBallCommand {

    val COMMAND_NAME = "8ball"
    val EIGHT_BALL_CHOICE = listOf<String>(
        "You may rely on it.",
        "Signs point to yes.",
        "Most likely.",
        "Very doubtful.",
        "My sources say no.",
        "Don’t count on it.",
        "Cannot predict now.",
        " Concentrate and ask again."
    )
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Roll an eight ball"), null
            )
        )
        registerToGlobalListeners()
    }
    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }
    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val eightBallChoice = EIGHT_BALL_CHOICE[Random.nextInt(0, EIGHT_BALL_CHOICE.size)]
        event.reply(eightBallChoice).queue()

    }
}