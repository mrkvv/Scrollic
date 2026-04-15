package com.example.scrollic.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrollic.R
import com.example.scrollic.design.*
import com.example.scrollic.screens.extra.GlassSheet
import com.example.scrollic.screens.extra.LoginSheetContent
import com.example.scrollic.screens.extra.RegistrationSheetContent
import kotlinx.coroutines.launch
import androidx.compose.foundation.LocalOverscrollConfiguration
import androidx.compose.foundation.overscroll
import com.example.scrollic.screens.extra.BottomSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    var isLogin by remember { mutableStateOf(true) }

    Box(
        modifier = Modifier.fillMaxSize()
    ) {

        Image(
            painter = painterResource(R.drawable.welcome_background),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 51.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {

            Spacer(modifier = Modifier.height(260.dp))

            Icon(
                imageVector = getIcon(IconType.LOGO),
                contentDescription = "Логотип",
                tint = Color.Unspecified
            )

            Spacer(modifier = Modifier.height(177.dp))

            GlassButton(
                text = "Регистрация",
                onClick = {
                    isLogin = false
                    showSheet = true
                }
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    BottomSheet(
        visible = showSheet,
        onDismiss = { showSheet = false }
    ) {
        if (isLogin) {
            GlassSheet {
                LoginSheetContent(
                    onLoginClick = {
                        showSheet = false
                        onLoginSuccess()
                    },
                    onRegisterClick = {
                        isLogin = false
                    }
                )
            }
        } else {
            GlassSheet {
                RegistrationSheetContent(
                    onRegisterClick = {
                        showSheet = false
                        onLoginSuccess()
                    },
                    onLoginClick = {
                        isLogin = true
                    }
                )
            }
        }
    }
}

@Composable
fun GlassButton(
    text: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .width(190.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Color.White.copy(alpha = 0.3f))
            .clickable { onClick() }
            .innerShadow(
                shape = RoundedCornerShape(30.dp),
                shadow = Shadow(
                    radius = 1.3.dp,
                    spread = 0.dp,
                    offset = DpOffset(1.dp, 2.dp),
                    color = Color.White,
                    alpha = 0.41f
                )
            )
            .innerShadow(
                shape = RoundedCornerShape(30.dp),
                shadow = Shadow(
                    radius = 4.dp,
                    spread = 0.dp,
                    offset = DpOffset((-2).dp, (-2).dp),
                    color = Color.White,
                    alpha = 0.25f
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = getInterFont(InterFontType.BOLD),
            fontSize = 24.sp,
            color = White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onLoginSuccess = {}
    )
}