package com.example.scrollic

import android.content.Context
import com.example.scrollic.database.DatabaseDriverFactory
import com.example.scrollic.database.LocalDatabaseManager
import com.example.scrollic.network.*

class AppContainer(private val context: Context) {

    private val databaseDriverFactory by lazy {
        DatabaseDriverFactory(context)
    }

    private val database by lazy {
        databaseDriverFactory.createDatabase()
    }

    private val dbManager by lazy {
        LocalDatabaseManager(database)
    }

    private val retrofitInstance by lazy {
        RetrofitInstance(dbManager)
    }

    private val apiService by lazy {
        retrofitInstance.apiService
    }

    // Сделали public (убрали private)
    val authManager by lazy {
        AuthManager(apiService, dbManager)
    }

    val feedManager by lazy {
        FeedManager(apiService)
    }

    val actionManager by lazy {
        ActionManager(apiService)
    }
}