package com.example.scrollic.screens

import androidx.compose.foundation.Image
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrollic.R
import com.example.scrollic.design.IconType
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.White
import com.example.scrollic.design.getIcon
import com.example.scrollic.design.getInterFont

@Composable
fun SettingsScreen(
    navController: NavController
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
    ){
        Image(
            painter = painterResource(R.drawable.settings_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Spacer(Modifier.height(60.dp))

            Text(
                text = "Настройки",
                fontFamily = getInterFont(InterFontType.MEDIUM),
                fontSize = 30.sp,
                color = White
            )

            Spacer(Modifier.height(60.dp))

            Text(
                text = "Сменить тему",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 24.sp,
                color = White
            )

            Spacer(Modifier.height(28.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                ThemeItem(
                    iconRes = R.drawable.theme_light,
                    title = "Светлая"
                )
                ThemeItem(
                    iconRes = R.drawable.theme_dark,
                    title = "Темная"
                )
                ThemeItem(
                    iconRes = R.drawable.theme_bright,
                    title = "Яркая"
                )
            }
        }

        Column (
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 40.dp, vertical = 60.dp)
        ){
            Row(
                modifier = Modifier
                    .clickable { navController.popBackStack() },
            ) {

                Icon(
                    imageVector = getIcon(IconType.ARROW_WHITE),
                    contentDescription = "Назад",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .graphicsLayer { rotationZ = 180f }
                        .size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = "Назад",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 22.sp,
                    color = White
                )
            }
        }
    }
}

@Composable
fun ThemeItem(
    iconRes: Int,
    title: String
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(id = iconRes),
            contentDescription = title,
            modifier = Modifier
                .width(70.dp)
                .height(70.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = title,
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 16.sp,
            color = White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SettingsScreenPreview() {

    SettingsScreen(
        navController = rememberNavController()
    )
}