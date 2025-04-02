package org.nocturne.commands
import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.Permission
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import org.nocturne.commands.CommandManager.adminUsers
import org.nocturne.database.DataBaseManager
import org.nocturne.database.GuildAttributeManager
import org.nocturne.listeners.GlobalListeners
import java.awt.Color


object  AdminAttributeConfigureCommand {
    val COMMAND_NAME = "adminconfigure"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Echo!")
                    .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
                    .addOption(OptionType.STRING, "attribute", "Attribute to configure" )
                    .addOption(OptionType.STRING, "value", "Attribute Value")
                , null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val attribute = event.getOption("attribute")
        val value = event.getOption("value")
        val guildId = event.guild?.idLong ?: return

        if (attribute == null || value == null) {
            handleShowPropertyList(event)
            return
        }
        DataBaseManager.genericAttributes
            .updateAttribute(value.asString,attribute.asString, guildId)

        event.reply("Updated").queue()
    }

    private fun handleShowPropertyList(event: SlashCommandInteractionEvent) {
        val guildId = event.guild?.idLong ?: return
        val attributes = DataBaseManager.genericAttributes.getAllGuildAttributes(guildId).executeAsList()

        val embedB = EmbedBuilder().setTitle("Attributes").setColor(Color.ORANGE)
        for (attr in attributes) {
            embedB.addField(MessageEmbed.Field("${attr.attribute_id} (${attr.type})",
                "${attr.data_}", true))
        }
        event.replyEmbeds(embedB.build()).queue()
    }


}
