package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.entities.Message
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import net.dv8tion.jda.api.interactions.components.ActionRow
import net.dv8tion.jda.api.interactions.components.buttons.Button
import net.dv8tion.jda.api.interactions.modals.Modal
import net.dv8tion.jda.api.interactions.components.text.TextInput
import net.dv8tion.jda.api.interactions.components.text.TextInputStyle
import java.awt.Color
import java.util.ArrayList
import java.util.function.Consumer
import javax.swing.text.StyleConstants.ColorConstants

object CommandManager {
    var adminUsers = arrayOf("393982976125435934", "321419785189720064","1347797953699647488")

    private var hasInit = false
    val commandMap = HashMap<String, MyCommand>()
    lateinit var mJda: JDA
    fun initializeCommands(jda: JDA) {
        if (hasInit) return
        hasInit = true
        this.mJda = jda

        mJda.addEventListener(GenericCommandListener())
        initSimpleCommands()
        registerAllCommandMapCommands()
    }

    /**
     * Queue commands to be initialized here, they will be added to the command map.
     */
    private fun initSimpleCommands() {
        // updateCommandMap(sendConfession())
        ConfessionCreateCommand.init()
        AdminEchoCommand.init()
        HelperReportCommand.init()
        EightBallCommand.init()
        FancyTextCommand.init()
        SocketTestCommand.init()
        ChatReviveCommand.init()
        // updateCommandMap(makeHelperTicket())
    }

    fun updateCommandMap(command: MyCommand): Boolean {
        commandMap[command.commandName.lowercase()] = command
        return true
    }

    /**
     * Take command map [command name] -> [MyCommand], and register every command to discord.
     */
    private fun registerAllCommandMapCommands() {
        val cmdData = ArrayList<CommandData>()
        for (cmd in commandMap.values) {
            cmdData.add(cmd.data)
        }
        if (cmdData.isEmpty()) return
        mJda.updateCommands().addCommands(cmdData).queue()
    }

    /**
     * WARNING --- SEE NEWER COMMAND INITIALIZATION. we should not need to invoke commands here.
     * This will handle all command interactions and route them to the correct callback.
     */
    class GenericCommandListener : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            var cmd = commandMap[event.name.lowercase()] ?: return
            if (cmd.commandName.isEmpty()) return
            cmd.commandCallback?.invoke(event)
        }
    }
}

/**
 * Command Name: The name the command to use
 * Data: Specify command name same as name here, and options/parameters for this command
 * CommandCallback: What to do when a user calls a command
 */
class MyCommand(
    var commandName: String,    // Leave name empty if we dont want to register command here.
    var data: CommandData,
    var commandCallback: ((SlashCommandInteractionEvent) -> Unit)?,
) {
}