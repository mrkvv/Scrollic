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
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
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
import com.example.scrollic.screens.extra.BottomSheet
import com.example.scrollic.screens.extra.EditGlassSheet
import com.example.scrollic.screens.extra.EditingProfileSheetContent
import com.example.scrollic.screens.extra.LoginSheetContent
import com.example.scrollic.screens.extra.RegistrationSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    navController: NavController
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

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
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(Modifier.height(263.dp))

            Text(
                text = "Венцеслав",
                fontFamily = getInterFont(InterFontType.MEDIUM),
                fontSize = 30.sp,
                color = White
            )

            Spacer(Modifier.height(5.dp))

            Text(
                text = "venceslav@dom2.ru",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 18.sp,
                color = White
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 40.dp, vertical = 60.dp)
        ) {
            Row(
                modifier = Modifier
                    .clickable { showSheet = true },
                verticalAlignment = Alignment.CenterVertically
            ) {

                Text(
                    text = "Редактировать профиль",
                    fontFamily = getInterFont(InterFontType.REGULAR),
                    fontSize = 22.sp,
                    color = White
                )

                Spacer(modifier = Modifier.width(8.dp))

                Icon(
                    imageVector = getIcon(IconType.ARROW_WHITE),
                    contentDescription = "Редактировать профиль",
                    tint = Color.Unspecified,
                    modifier = Modifier
                        .size(24.dp)
                )
            }

            Spacer(Modifier.height(25.dp))

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

    BottomSheet(
        visible = showSheet,
        onDismiss = { showSheet = false }
    ) {
        EditGlassSheet {
            EditingProfileSheetContent(
                onBackClick = { showSheet = false }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {

    ProfileScreen(
        navController = rememberNavController()
    )
}