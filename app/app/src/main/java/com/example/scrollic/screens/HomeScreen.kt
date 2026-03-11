package com.example.scrollic.screens

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.navigation.NavController
import com.example.scrollic.navigation.Screen
import com.example.scrollic.screens.extra.DrawerContent
import kotlin.math.roundToInt
import com.example.scrollic.R

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
    ) {
        DrawerContent(
            modifier = Modifier
                .zIndex(0f)
                .alpha(if (isMenuOpen) 1f else 0f),
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
                .zIndex(1f)
                .fillMaxSize()
                .offset { IntOffset(offsetX.roundToInt(), 0) }
                .scale(scale)
                .clip(RoundedCornerShape(radius))
                .clickable(
                    enabled = isMenuOpen,
                    indication = null,
                    interactionSource = remember { MutableInteractionSource() }
                ) {
                    isMenuOpen = false
                }
        ) {
            Image(
                painter = painterResource(R.drawable.main_background),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )


            Column {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 25.dp, top = 60.dp)
                ){
                    IconButton(
                        onClick = {
                            isMenuOpen = !isMenuOpen
                        }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.menu_icon),
                            contentDescription = "Меню",
                            tint = Color.Unspecified
                        )
                    }

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