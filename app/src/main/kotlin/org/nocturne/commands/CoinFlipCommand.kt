package org.nocturne.commands

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.database.DataBaseManager.USER_PROFILE
import org.nocturne.listeners.GlobalListeners
import kotlin.math.roundToInt
import kotlin.random.Random

object CoinFlipCommand {
    val COMMAND_NAME = "coinflip"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Bet your lunaris away")
                    .addOption(OptionType.INTEGER, "bet", "What to bet",true), null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val betOpt = event.getOption("bet")?: return
        val user = USER_PROFILE.selectUserByUserId(event.user.idLong).executeAsOneOrNull()
        val betAmt = betOpt.asLong


        if(betAmt > user!!.lunaris) {
            event.reply("You do not have enough :Lunaris: **Lunaris** for that").queue()
            return
        }
        if (betAmt <= 0) {
            event.reply("You tried.... Use a positive non-zero bet.").queue()
            return
        }
        if (Random.nextBoolean()) {
            val amount = betOpt
            event.reply("You won :Lunaris: ${amount.asInt} **Lunaris**").queue()
                USER_PROFILE.updateLunaris(amount.asLong+user.lunaris,event.user.idLong)
                return
        }
        val amount = betOpt
        event.reply("You lost :Lunaris: ${amount.asInt} **Lunaris**").queue()
        USER_PROFILE.updateLunaris(user.lunaris-amount.asLong,event.user.idLong)


    }
}