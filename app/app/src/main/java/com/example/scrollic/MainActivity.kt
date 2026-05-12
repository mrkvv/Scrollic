package com.example.scrollic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.scrollic.AppContainer
import com.example.scrollic.navigation.AppNavGraph
import com.example.scrollic.network.FeedViewModel

class MainActivity : ComponentActivity() {

    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        appContainer = AppContainer(applicationContext)

        setContent {
            AppNavGraph(
                appContainer = appContainer  // Передаем контейнер, а не ViewModel
            )
        }
    }
}