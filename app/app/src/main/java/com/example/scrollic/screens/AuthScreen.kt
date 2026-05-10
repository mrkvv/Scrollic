package com.example.scrollic.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.scrollic.R
import com.example.scrollic.design.*
import com.example.scrollic.screens.extra.*
import com.example.scrollic.network.AuthUiState
import com.example.scrollic.network.AuthViewModel
import com.example.scrollic.ui.GlassButton
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuthScreen(
    onLoginSuccess: () -> Unit,
    authViewModel: AuthViewModel
) {
    val sheetState = rememberModalBottomSheetState(
        skipPartiallyExpanded = true
    )
    val scope = rememberCoroutineScope()
    val uiState by authViewModel.uiState.collectAsState()

    var showSheet by remember { mutableStateOf(false) }
    var isLogin by remember { mutableStateOf(true) }

    LaunchedEffect(uiState) {
        when (uiState) {
            is AuthUiState.Success -> {
                showSheet = false
                onLoginSuccess()
                authViewModel.resetState()
            }
            is AuthUiState.Error -> {
                android.util.Log.e("AuthScreen", (uiState as AuthUiState.Error).message)
            }
            else -> {}
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
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
                    authViewModel.resetState()
                }
            )

            Spacer(modifier = Modifier.height(60.dp))
        }
    }

    BottomSheet(
        visible = showSheet,
        onDismiss = {
            showSheet = false
            authViewModel.resetState()
        }
    ) {
        if (isLogin) {
            LoginSheetContent(
                isLoading = uiState is AuthUiState.Loading,
                errorMessage = (uiState as? AuthUiState.Error)?.message,
                onLoginClick = { name, password ->
                    authViewModel.login(name, password)
                },
                onRegisterClick = {
                    isLogin = false
                    authViewModel.resetState()
                }
            )
        } else {
            RegistrationSheetContent(
                isLoading = uiState is AuthUiState.Loading,
                errorMessage = (uiState as? AuthUiState.Error)?.message,
                onRegisterClick = { name, password ->
                    println("DEBUG: AuthScreen received register: $name")
                    authViewModel.register(name, password)
                },
                onLoginClick = {
                    isLogin = true
                    authViewModel.resetState()
                }
            )
        }
    }
}