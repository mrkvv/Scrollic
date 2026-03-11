package com.example.scrollic.screens.extra

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
import com.example.scrollic.R
import com.example.scrollic.design.IconType
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.White
import com.example.scrollic.design.getIcon
import com.example.scrollic.design.getInterFont
import com.example.scrollic.navigation.Screen
import com.example.scrollic.screens.AuthScreen

@Composable
fun DrawerContent(
    modifier: Modifier = Modifier,
    onItemClick: (String) -> Unit,
    onLogoutClick: () -> Unit
) {

    Box(modifier = Modifier.fillMaxSize()) {

        Image(
            painter = painterResource(R.drawable.settings_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = modifier
                .fillMaxHeight()
                .width(260.dp)
                .padding(horizontal = 45.dp, vertical = 110.dp)
        ) {

            Spacer(modifier = Modifier.height(125.dp))

            Text(
                text = "Венцеслав",
                fontFamily = getInterFont(InterFontType.MEDIUM),
                fontSize = 30.sp,
                color = White
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

            DrawerItem("О нас") {
                onItemClick(Screen.Info.route)
            }

            Spacer(modifier = Modifier.weight(1f))

            Row(
                modifier = Modifier
                    .clickable { onLogoutClick() }
                    .padding(vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Выйти",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 20.sp,
                    color = White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = getIcon(IconType.ARROW_WHITE),
                    contentDescription = "Выйти",
                    tint = Color.Unspecified,
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
fun DrawerItem(
    title: String,
    onClick: () -> Unit
) {
    Text(
        text = title,
        fontFamily = getInterFont(InterFontType.REGULAR),
        fontSize = 20.sp,
        color = White,
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 12.dp)
    )
}

@Preview(showBackground = true)
@Composable
fun DrawerPreview() {

    DrawerContent(
        onItemClick = {},
        onLogoutClick = {}
    )
}
