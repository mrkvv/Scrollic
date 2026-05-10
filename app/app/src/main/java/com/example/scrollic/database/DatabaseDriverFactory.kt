package com.example.scrollic.database

import android.content.Context
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.example.scrollic.localDb.LocalDatabase

class DatabaseDriverFactory(private val context: Context) {

    fun createDriver(): SqlDriver {
        return AndroidSqliteDriver(
            schema = LocalDatabase.Schema,
            context = context,
            name = "scrollic.db"
        )
    }

    fun createDatabase(): LocalDatabase {
        val driver = createDriver()
        return LocalDatabase(driver)
    }
}