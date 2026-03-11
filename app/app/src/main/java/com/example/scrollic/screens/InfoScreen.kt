package com.example.scrollic.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.*
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
import com.example.scrollic.design.IconType
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.Pink
import com.example.scrollic.design.White
import com.example.scrollic.design.getIcon
import com.example.scrollic.design.getInterFont
import com.example.scrollic.R

@Composable
fun InfoScreen(
    navController: NavController
) {

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.settings_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 40.dp, vertical = 60.dp)
        ) {

            Text(
                text = "О нас",
                fontFamily = getInterFont(InterFontType.MEDIUM),
                fontSize = 30.sp,
                color = White
            )

            Spacer(modifier = Modifier.height(35.dp))

            Text(
                text = "Мы команда ПАЛКа, ответственная за Мобильное приложение \"Лента новостей Скроллик\".\n\n" +
                        "Это проект в рамках учебной дисциплины \"Архитектура программных систем\". " +
                        "Его основная задача - решение возникающих архитектурных вопросов. " +
                        "Второстепенная задача - разработка мобильного приложения ленты новостей Scrollic.\n\n" +
                        "Команда разработки состоит из следующих ролей:\n" +
                        "TeamLead, он же Arch\nBackend\nDevOps\nMobile",

                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = White
            )

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .clickable { navController.popBackStack() },
                verticalAlignment = Alignment.CenterVertically
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

@Preview(showBackground = true)
@Composable
fun InfoScreenPreview() {

    InfoScreen(
        navController = rememberNavController()
    )
}