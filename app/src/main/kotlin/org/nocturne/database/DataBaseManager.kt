package org.nocturne.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import org.nocturne.UserProfileQueries

object DataBaseManager {

    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:nocturne.db")
    val USER_PROFILE = UserProfileQueries(driver)

}