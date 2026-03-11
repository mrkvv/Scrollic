package com.example.scrollic.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrollic.R
import com.example.scrollic.design.*
import com.example.scrollic.screens.extra.LoginSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
) {

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

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

            Button(
                onClick = { showSheet = true },
                modifier = Modifier
                    .width(190.dp)
                    .height(60.dp)
            ) {
                Text(
                    text = "Вход",
                    fontFamily = getInterFont(InterFontType.BOLD),
                    fontSize = 24.sp,
                    color = White
                )
            }

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    if (showSheet) {

        ModalBottomSheet(
            onDismissRequest = { showSheet = false },
            sheetState = sheetState,
            containerColor = Color.White
        ) {

            LoginSheetContent(
                onLoginClick = {
                    scope.launch {
                        sheetState.hide()
                        showSheet = false
                        onLoginSuccess()
                    }
                }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthScreenPreview() {
    AuthScreen(
        onLoginSuccess = {}
    )
}