package org.nocturne.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.nocturne.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object DataBaseManager {
    var logger: Logger = LoggerFactory.getLogger(DataBaseManager::class.java)
    lateinit var USER_DRIVER: SqlDriver
    lateinit var GUILD_DRIVER: SqlDriver

    //todo add to gitignore
    lateinit var USER_PROFILE: UserProfileQueries
    lateinit var REACT_MILESTONE: ReactMilestoneQueries
    lateinit var genericAttributes: GenericAttributeStorageQueries

    fun init() {
        try {
            logger.info("Initializing Database")
            USER_DRIVER = JdbcSqliteDriver("jdbc:sqlite:../../user.db")
            GUILD_DRIVER = JdbcSqliteDriver("jdbc:sqlite:../../guild.db")

            // Initialize tables
            try {
                NocturneDB.Schema.create(GUILD_DRIVER)
            } catch (ignored: Exception) {}
            try {
                NocturneDB.Schema.create(USER_DRIVER)
            } catch (ignored: Exception) {}
            USER_PROFILE = UserProfileQueries(USER_DRIVER)
            REACT_MILESTONE = ReactMilestoneQueries(GUILD_DRIVER)
            genericAttributes = GenericAttributeStorageQueries(GUILD_DRIVER)

        } catch (e: Exception) {
            logger.error(e.message)
            logger.error(e.stackTraceToString())
        }

    }
}