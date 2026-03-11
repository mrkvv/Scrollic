package com.example.scrollic.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrollic.design.IconType
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.White
import com.example.scrollic.design.getIcon
import com.example.scrollic.design.getInterFont

@Composable
fun InterestsScreen(
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
                text = "Интересы",
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

@Preview(showBackground = true)
@Composable
fun InterestsScreenPreview() {

    InterestsScreen(
        navController = rememberNavController()
    )
}