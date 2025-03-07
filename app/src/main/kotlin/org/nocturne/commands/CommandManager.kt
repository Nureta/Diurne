package org.nocturne.commands

import net.dv8tion.jda.api.JDA
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.hooks.ListenerAdapter
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.CommandData
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import java.util.ArrayList

object CommandManager {
    private var hasInit = false
    private val commandMap = HashMap<String, MyCommand>()
    lateinit var mJda: JDA
    fun initializeCommands(jda: JDA) {
        if (hasInit) return
        this.mJda = jda
        mJda.addEventListener(GenericCommandListener())
        initSimpleCommands()
        registerAllCommandMapCommands()
    }

    private fun initSimpleCommands() {
        val simplePingCommand = MyCommand(
            "animal",
            Commands.slash("animal", "Finds a random animal")
                .addOptions(
                    OptionData(OptionType.STRING, "type", "The type of animal to find")
                        .addChoice("Bird", "bird")
                        .addChoice("Big Cat", "bigcat")
                        .addChoice("Canine", "canine")
                        .addChoice("Fish", "fish")
                )
        ) { event ->
            event.reply(event.getOption("type")!!.asString).queue() // reply immediately
        }

        updateCommandMap(simplePingCommand)
    }

    private fun updateCommandMap(command: MyCommand): Boolean {
        commandMap[command.commandName] = command
        return true
    }

    private fun registerAllCommandMapCommands() {
        val cmdData = ArrayList<CommandData>()
        for (cmd in commandMap.values) {
            cmdData.add(cmd.data)
        }
        if (cmdData.isEmpty()) return
        mJda.updateCommands().addCommands(cmdData).queue()
    }

    class GenericCommandListener : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            var cmd = commandMap[event.name]
            if (cmd == null) return
            cmd.commandCallback(event)
        }
    }
}

class MyCommand(
    var commandName: String,
    var data: CommandData,
    var commandCallback: ((SlashCommandInteractionEvent) -> Unit)
)