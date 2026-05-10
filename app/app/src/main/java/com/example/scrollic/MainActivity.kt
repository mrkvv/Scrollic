package com.example.scrollic

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.scrollic.di.AppContainer
import com.example.scrollic.navigation.AppNavGraph

class MainActivity : ComponentActivity() {

    // Создаем контейнер зависимостей
    private lateinit var appContainer: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Инициализируем контейнер с зависимостями
        appContainer = AppContainer(applicationContext)

        setContent {
            // Передаем ViewModel из контейнера в навигацию
            AppNavGraph(authViewModel = appContainer.authViewModel)
        }
    }
}