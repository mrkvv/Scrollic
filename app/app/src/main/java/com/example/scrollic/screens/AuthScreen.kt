package com.example.scrollic.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.scrollic.screens.extra.LoginSheetContent
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit
){

    val sheetState = rememberModalBottomSheetState()
    val scope = rememberCoroutineScope()

    var showSheet by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF2F2F2))
            .padding(vertical = 350.dp),
        contentAlignment = Alignment.BottomCenter
    ) {
        Button(
            onClick = {
                showSheet = true
            }
        ) {
            Text("Вход")
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