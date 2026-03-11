package com.example.scrollic.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.White
import com.example.scrollic.design.getInterFont

@Composable
fun ProfileScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(Modifier.height(340.dp))

            Text(
                text = "Профиль",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(Modifier.height(40.dp))

            Button(
                onClick = { navController.popBackStack() },
                modifier = Modifier
                    .width(190.dp)
                    .height(60.dp)
            ) {
                Text(
                    text = "Назад",
                    fontFamily = getInterFont(InterFontType.BOLD),
                    fontSize = 24.sp,
                    color = White
                )
            }
        }
    }
}