package com.example.scrollic.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.scrollic.screens.AuthScreen
import com.example.scrollic.screens.HomeScreen
import com.example.scrollic.screens.InfoScreen
import com.example.scrollic.screens.InterestsScreen
import com.example.scrollic.screens.ProfileScreen
import com.example.scrollic.screens.SettingsScreen
import com.example.scrollic.network.AuthViewModel

@Composable
fun AppNavGraph(
    authViewModel: AuthViewModel
) {
    val navController = rememberNavController()

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
            HomeScreen(navController)
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