package com.example.scrollic.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scrollic.AppContainer
import com.example.scrollic.screens.AuthScreen
import com.example.scrollic.screens.HomeScreen
import com.example.scrollic.screens.InfoScreen
import com.example.scrollic.screens.InterestsScreen
import com.example.scrollic.screens.ProfileScreen
import com.example.scrollic.screens.SettingsScreen
import com.example.scrollic.network.AuthViewModel
import com.example.scrollic.network.FeedViewModel

@Composable
fun AppNavGraph(
    appContainer: AppContainer
) {
    val navController = rememberNavController()

    // Создаем ViewModel через remember
    val authViewModel = remember {
        AuthViewModel(appContainer.authManager)
    }

    val feedViewModel = remember {
        FeedViewModel(appContainer.feedManager)
    }

    NavHost(
        navController = navController,
        startDestination = Screen.Auth.route
    ) {
        composable(Screen.Auth.route) {
            AuthScreen(
                onLoginSuccess = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Auth.route) {
                            inclusive = true
                        }
                    }
                },
                authViewModel = authViewModel
            )
        }

        composable(Screen.Home.route) {
            HomeScreen(
                navController = navController,
                feedViewModel = feedViewModel
            )
        }

        composable(Screen.Profile.route) {
            ProfileScreen(navController)
        }

        composable(Screen.Settings.route) {
            SettingsScreen(navController)
        }

        composable(Screen.Interests.route) {
            InterestsScreen(navController)
        }

        composable(Screen.Info.route) {
            InfoScreen(navController)
        }
    }
}