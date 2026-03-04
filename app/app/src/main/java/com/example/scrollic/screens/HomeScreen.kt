package com.example.scrollic.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.scrollic.navigation.Screen
import com.example.scrollic.screens.extra.DrawerContent
import kotlin.math.roundToInt

@Composable
fun HomeScreen(navController: NavController) {

    var isMenuOpen by remember { mutableStateOf(false) }

    val transition = updateTransition(isMenuOpen, label = "drawer")

    val offsetX by transition.animateFloat(label = "offset") {
        if (it) 600f else 0f
    }

    val scale by transition.animateFloat(label = "scale") {
        if (it) 0.85f else 1f
    }

    val radius by transition.animateDp(label = "radius") {
        if (it) 24.dp else 0.dp
    }

    Box(
        modifier = Modifier.fillMaxSize()
            .background(Color(0xFF1E1E2E))
    ) {

        DrawerContent(
            onItemClick = { route ->
                isMenuOpen = false
                navController.navigate(route)
            },
            onLogoutClick = {
                navController.navigate(Screen.Auth.route) {
                    popUpTo(Screen.Home.route) {
                        inclusive = true
                    }
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .scale(scale)
                .clip(RoundedCornerShape(radius))
                .background(Color(0xFFF5F5F5))
        ) {

            Column {

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp, horizontal = 12.dp)
                ){
                    Text(
                        text = "☰",
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.clickable {
                            isMenuOpen = !isMenuOpen
                        }
                    )

                    Spacer(modifier = Modifier.width(16.dp))
                }

                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text("Лента новостей")
                }
            }
        }
    }

}