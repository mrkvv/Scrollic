package com.example.scrollic.di

import android.content.Context
import com.example.scrollic.database.DatabaseDriverFactory
import com.example.scrollic.database.LocalDatabaseManager
import com.example.scrollic.network.AuthManager
import com.example.scrollic.network.RetrofitInstance
import com.example.scrollic.network.AuthViewModel

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

    private val authManager by lazy {
        AuthManager(apiService, dbManager)
    }

    val authViewModel by lazy {
        AuthViewModel(authManager)
    }
}