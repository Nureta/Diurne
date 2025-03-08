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
    private val commandMap = HashMap<String, MyCommand>()
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
        updateCommandMap(getAnimalCommand())
        updateCommandMap(getEchoCommand())
        updateCommandMap(getPokemonCommand())
        updateCommandMap(sendConfession())
        updateCommandMap(makeHelperTicket())

    }


    // region Commands

    /**
     * Example command
     * @see MyCommand
     * Replies to whatever user responds with the same animal back.
     */
    private fun getAnimalCommand(): MyCommand {
        return MyCommand(
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
    }

    private fun getEchoCommand(): MyCommand {
        // Echo Command
        return MyCommand(
            "adminecho",
            Commands.slash("adminecho", "Echo!")
                .addOption(OptionType.STRING, "msg", "What to Echo")
        ) { event ->
            val sender = event.member?.user?.id ?: return@MyCommand
            if (!adminUsers.contains(sender)) return@MyCommand
            val echoOpt = event.getOption("msg") ?: return@MyCommand

            println("ECHO COMMAND - [${event.getChannel()}] ${event.member?.user?.name ?: "Unknown User"}: ${echoOpt}\n")
            event.channel.sendMessage(echoOpt.asString).queue(object : Consumer<Message> {
                override fun accept(msg: Message) {
                    System.out.printf("Sent Message %s\n", msg);
                    event.reply("Success").setEphemeral(true).queue()
                }
            })
        }
    }

    private fun getPokemonCommand(): MyCommand {
        return MyCommand(
            "pokemon",
            Commands.slash("pokemon", "Finds a random animal")
                .addOptions(
                    OptionData(OptionType.STRING, "type", "The type of animal to find")
                        .addChoice("Pikachu", "Pika")
                        .addChoice("MewTwo", "MewThree")
                        .addChoice("Piplup", "piplup")
                        .addChoice("Fish", "fish")
                )
        ) { event ->
            event.reply(event.getOption("type")!!.asString).queue() // reply immediately
        }
    }

    private fun sendConfession(): MyCommand {
        return MyCommand(
            "confession",
            Commands.slash("confession","Send a anonymous confession")

        )
        {event ->
            val confessionInput = TextInput.create("confession","confession", TextInputStyle.PARAGRAPH)
                .setPlaceholder("Confession")
                .setMaxLength(400)
                .setMinLength(10)
                .build()
            val confessionModal = Modal.create("confession","Confession")
                .addComponents(ActionRow.of(confessionInput))
                .build()
            event.replyModal(confessionModal).queue()
        }
    }

    private fun makeHelperTicket(): MyCommand {
        return MyCommand(
            "setuphelper",
            Commands.slash("setuphelper","Assign helper ticket to a channel")
     )
        {event ->
            val sender = event.member?.user?.id ?: return@MyCommand
            if (!adminUsers.contains(sender)) return@MyCommand
            val helperEmbed = EmbedBuilder()
                .setTitle("Create a helper report")
                .setDescription("Provide attachments/proof, reason, and decision to made. \n IF none is provided logical reasoning is permitted/context")
                .setFooter("Vote for whether or not to invoke this executive decision")
                .setColor(Color(0x9efffd))
                .build()
            event.guild!!.getTextChannelById("1347819027858194473")!!.sendMessageEmbeds(helperEmbed)
                .addActionRow(
                    Button.primary("report","Submit a report!")
                )
                .queue()
            event.reply("Message sent to assigned Channel!").setEphemeral(true)
        }

    }


    // endregion


    private fun updateCommandMap(command: MyCommand): Boolean {
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
     * This will handle all command interactions and route them to the correct callback
     */
    class GenericCommandListener : ListenerAdapter() {
        override fun onSlashCommandInteraction(event: SlashCommandInteractionEvent) {
            var cmd = commandMap[event.name.lowercase()]
            if (cmd == null) return
            cmd.commandCallback(event)
        }
    }
}

/**
 * Command Name: The name the command to use
 * Data: Specify command name same as name here, and options/parameters for this command
 * CommandCallback: What to do when a user calls a command
 */
class MyCommand(
    var commandName: String,
    var data: CommandData,
    var commandCallback: ((SlashCommandInteractionEvent) -> Unit)
)