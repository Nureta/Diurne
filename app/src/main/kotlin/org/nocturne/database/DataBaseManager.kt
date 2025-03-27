package org.nocturne.database

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver

object DataBaseManager {
    val driver: SqlDriver = JdbcSqliteDriver("jdbc:sqlite:test.db")

}