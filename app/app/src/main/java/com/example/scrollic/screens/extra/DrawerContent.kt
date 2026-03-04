package com.example.scrollic.screens.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scrollic.navigation.Screen

@Composable
fun DrawerContent(
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxHeight()
            .width(260.dp)
            .background(Color(0xFF2A2A40))
            .padding(horizontal = 48.dp, vertical = 200.dp)
    ){

        Text(
            text = "Венцеслав",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White
        )

        Spacer(modifier = Modifier.height(40.dp))

        DrawerItem("Профиль") {
            onItemClick(Screen.Profile.route)
        }

        DrawerItem("Настройки") {
            onItemClick(Screen.Settings.route)
        }

        DrawerItem("Интересы") {
            onItemClick(Screen.Interests.route)
        }

        DrawerItem("Помощь") {
            onItemClick(Screen.Help.route)
        }

        Spacer(modifier = Modifier.weight(1f))

        DrawerItem("Выйти", onClick = onLogoutClick)
    }
}

@Composable
fun DrawerItem(
    title: String,
    onClick: () -> Unit
) {
    Text(
        text = title,
        color = Color.White,
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    )
}