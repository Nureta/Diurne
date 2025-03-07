package org.nocturne.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.ArrayList

object CommandManager
{
    private var hasInit = false
    private val mySimpleCommandList = ArrayList<MyCommand>();
    private val commandMap = HashMap<String, MyCommand>()
    lateinit var mJda: JDA
    fun InitializeCommands(jda: JDA) {
        if (hasInit) return
        this.mJda = jda
        mJda.addEventListener(GenericCommandListener())
        initSimplecommands()
        registerAllCommandMapCommands()
    }

    private fun initSimplecommands() {
        val simplePingCommand = MyCommand()
        simplePingCommand.commandName = "animal"
        simplePingCommand.commandCallback = { event ->
            event.reply(event.getOption("type")!!.asString).queue() // reply immediately
        }
        simplePingCommand.data = Commands.slash(simplePingCommand.commandName!!, "Finds a random animal")
            .addOptions(
                OptionData(OptionType.STRING, "type", "The type of animal to find")
                    .addChoice("Bird", "bird")
                    .addChoice("Big Cat", "bigcat")
                    .addChoice("Canine", "canine")
                    .addChoice("Fish", "fish")
            )
        updateCommandMap(simplePingCommand)
    }

    private fun updateCommandMap(command: MyCommand): Boolean {
        if (command.commandName == null || command.commandName.isNullOrEmpty()) return false
        commandMap[command.commandName!!] = command
        return true
    }

    private fun registerAllCommandMapCommands() {
        val cmdData = ArrayList<CommandData>()
        for (cmd in commandMap.values) {
            if (cmd.data == null) continue
            cmdData.add(cmd.data!!)
        }
        if (cmdData.isEmpty()) return
        mJda.updateCommands().addCommands(cmdData).queue()
    }

    class GenericCommandListener : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            var cmd = commandMap[event.name]
            if (cmd == null || cmd.commandCallback == null) return
            cmd.commandCallback!!(event)
        }
    }
}

class MyCommand {
    var commandName: String? = null
    var commandCallback: ((SlashCommandInteractionEvent) -> Unit)? = null
    var data: CommandData? = null
}