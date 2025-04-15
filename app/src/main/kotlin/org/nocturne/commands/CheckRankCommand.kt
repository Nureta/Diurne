package org.nocturne.commands

import net.dv8tion.jda.api.EmbedBuilder
import net.dv8tion.jda.api.entities.EmbedType
import net.dv8tion.jda.api.entities.MessageEmbed
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions
import net.dv8tion.jda.api.interactions.commands.OptionType
import net.dv8tion.jda.api.interactions.commands.build.Commands
import net.dv8tion.jda.api.interactions.commands.build.OptionData
import org.nocturne.UserProfile
import org.nocturne.listeners.GlobalListeners
import org.nocturne.logic.leveling.LevelingManager
import java.awt.Color
import org.nocturne.database.DataBaseManager.USER_PROFILE

object CheckRankCommand {
    val COMMAND_NAME = "rank"
    private var hasInit = false

    fun init() {
        if (hasInit) return
        hasInit = true
        CommandManager.updateCommandMap(
            MyCommand(
                COMMAND_NAME, Commands.slash(COMMAND_NAME, "Checks the user's rank")
                    .addOption(OptionType.USER,"user","User to check", false)
                    .setDefaultPermissions(DefaultMemberPermissions.ENABLED)
                ,null
            )
        )
        registerToGlobalListeners()
    }

    private fun registerToGlobalListeners() {
        GlobalListeners.onSlashCommandInteractionSubscribers[COMMAND_NAME] = ::onSlashCommand
    }

    private fun onSlashCommand(event: SlashCommandInteractionEvent) {
        val sortedUsers = USER_PROFILE.selectUserSortedByLevelDesc().executeAsList()
        var userProfile: UserProfile?
        val userOpt = event.getOption("user")?.asMember

        val userID: Long
        if (userOpt != null) {
            userID = userOpt.idLong
        } else {
            userID = event.user.idLong
        }
        userProfile = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()

        // Init user if null
        if (userProfile == null) {
            USER_PROFILE.insertUser(userID, 0, 0, 0, 1.0, 0)
            userProfile = USER_PROFILE.selectUserByUserId(userID).executeAsOneOrNull()!!
        }

        // Retrieve user member object
        var userMember = event.guild?.getMemberById(userProfile.user_id)
        if (userMember == null) {
            userMember = event.guild?.retrieveMemberById(userProfile.user_id)?.complete()
            if (userMember == null){
                event.reply("User not found").setEphemeral(true).queue()
                return
            }
        }
        // Create Embed
        var lunarisStr = ":coin:"
        if (event.guild != null && event.guild!!.idLong == 1325079345521365133) {
            lunarisStr = "<:Lunaris:1352820067087155232>"
        }
        var rankDescription = "━━━━⊱⋆⊰━━━━\n" +
                "Lvl. ${userProfile.current_level}\n-# ${userProfile.experience}/${
            LevelingManager.nextLevelReq(
                (userProfile.current_level + 1))}"
        if (userMember.isBoosting) {
            rankDescription += " [+50% Booster Bonus]"
        }
        rankDescription += "\n $lunarisStr **Lunaris** ${userProfile.lunaris}\n" +
                "━━━━⊱⋆⊰━━━━"
        var userRankEmbed: MessageEmbed = EmbedBuilder()
            .setColor(Color(115, 138, 255))
            .setAuthor(userMember.user.name, null, userMember.user.effectiveAvatarUrl)
            .setTitle("Rank: `${sortedUsers.indexOf(userProfile) + 1}`")
            .setDescription("━━━━━⋆━━━━━⊱⋆⊰━━━━━⋆━━━━━")
            .addField("LVL: `${userProfile.current_level}`", "", true)
            .addField("EXP: `${userProfile.experience}/${LevelingManager.nextLevelReq((userProfile.current_level + 1))}`",
                "", true)
            .addField("Lunaris: `${userProfile.lunaris}` $lunarisStr",
                "", true)
            .build()
        event.replyEmbeds(userRankEmbed).queue()
        return
    }
}