package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.emoji.Emoji
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.listeners.GlobalListeners
import java.awt.Color


object FancyTextCommand {
    val EMOJI_LIST = listOf<String> (
        "<:A_Nocturne:1352820232250724414>",
        "<:B_Nocturne:1352822905813205023>",
        "<:C_Nocturne:1352822708676595843>",
        "<:D_Nocturne:1352823849867022366>",
        "<:E_Nocturne:1352823504956952708>",
        "<:F_Nocturne:1352822907910225981>",
        "<:G_Nocturne:1352822901841330196>",
        "<:H_Nocturne:1352820230614679674>",
        "<:I_Nocturne:1352822898842406942>",
        "<:J_Nocturne:1352822894501171272>",
        "<:K_Nocturne:1352822897500229714>",
        "<:L_Nocturne:1352820228001759322>",
        "<:M_Nocturne:1352820226928152576>",
        "<:N_Nocturne:1352820235270619207>",
        "<:O_Nocturne:1352822896250064987>",
        "<:P_Nocturne:1352820238051180564>",
        "<:Q_Nocturne:1352820229117313045>",
        "<:R_Nocturne:1352820225447297045>",
        "<:S_Nocturne:1352822903217066044>",
        "<:T_Nocturne:1352820223950196738>",
        "<:U_Nocturne:1352820242069323866>",
        "<:V_Nocturne:1352820245596999741>",
        "<:W_Nocturne:1352822900092309585>",
        "<:X_Nocturne:1352822733591019540>",
        "<:Y_Nocturne:1352820233613738044>",
        "<:Z_Nocturne:1352822776804675585>"
        )

    val COMMAND_NAME = "fancytext"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Make your text fancy!")
                    .addOption(OptionType.STRING, "msg", "What to convert"), null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val fancyOpt = event.getOption("msg")?.asString ?: return
        var newText = ""
        var alphabetNumber = 0
        for ( x in fancyOpt) {
            if (!x.isLetter()) {
                if (x == ' ') {
                    newText += "    "
                    continue
                } else {
                    continue
                }
            }
            alphabetNumber = x.code - 97
            if (x.isUpperCase()) {
                alphabetNumber += 32
                if (alphabetNumber <= EMOJI_LIST.size) {
                    newText = newText.plus(EMOJI_LIST[alphabetNumber])
                }
            } else {
                if (alphabetNumber <= EMOJI_LIST.size) {
                    newText = newText.plus(EMOJI_LIST[alphabetNumber])
                }
            }
        }
        val fancyEmbed = EmbedBuilder()
            .setTitle("New Fancy Text")
            .setColor(Color(106,159,235))
            .setDescription(fancyOpt)
            .setFooter("From ${event.member?.user?.name}")
            .build()
        event.channel.sendMessage(newText).addEmbeds(fancyEmbed).queue()
        event.reply("Success").setEphemeral(true).queue()

    }


}