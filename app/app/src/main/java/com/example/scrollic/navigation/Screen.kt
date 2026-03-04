package com.example.scrollic.navigation

sealed class Screen(val route: String) {

    object Auth : Screen("auth")
    object Home : Screen("home")
    object Profile: Screen("profile")
    object Settings : Screen("settings")
    object Interests : Screen("interests")
    object Help : Screen("help")
}