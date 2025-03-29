package org.nocturne.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.nocturne.*

object DataBaseManager {

    lateinit var USER_DRIVER: SqlDriver
    lateinit var GUILD_DRIVER: SqlDriver
    //todo add to gitignore
    lateinit var USER_PROFILE: UserProfileQueries
    lateinit var REACT_MILESTONE: ReactMilestoneQueries

    fun init() {
        try {
            USER_DRIVER = JdbcSqliteDriver("jdbc:sqlite:user.db")
            GUILD_DRIVER = JdbcSqliteDriver("jdbc:sqlite:guild.db")
            NocturneDB.Schema.create(DataBaseManager.GUILD_DRIVER)
            NocturneDB.Schema.create(DataBaseManager.USER_DRIVER)
            USER_PROFILE = UserProfileQueries(USER_DRIVER)
            REACT_MILESTONE = ReactMilestoneQueries(GUILD_DRIVER)
            } catch (ignored: Exception) {
        }

    }
}