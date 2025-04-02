package org.nocturne.database

object GuildAttributeManager {
    val CHANNEL_TYPE = "channel"

    // Pair<Attribute, Type>
    private var guildAttributes = ArrayList<Pair<String, String?>>()

    fun initGuild(guild: Long) {
        for (attr in guildAttributes) {
            initalizeAttribute(guild, attr.first, attr.second)
        }
    }

    fun addDefaultGuildAttribute(attribute: String, type: String?) {
        guildAttributes.add(Pair(attribute, type))
    }

    private fun initalizeAttribute(guild: Long, attr: String, type: String?) {
        val attrVal = DataBaseManager.genericAttributes
            .getAttribute(attr, guild).executeAsOneOrNull()
        if (attrVal != null) return
        DataBaseManager.genericAttributes
            .addOrUpdateAttribute(attr, guild, type, null)
    }
}