package com.example.scrollic.screens.extra

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrollic.design.*
import com.example.scrollic.ui.AuthButton
import com.example.scrollic.ui.AuthLinkText

@Composable
fun RegistrationSheetContent(
    isLoading: Boolean,
    errorMessage: String?,
    onRegisterClick: (name: String, password: String) -> Unit,
    onLoginClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Регистрация",
            fontFamily = getInterFont(InterFontType.SEMI_BOLD),
            fontSize = 30.sp,
            color = Pink
        )

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "Имя",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = LightGrey
        )

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(27.dp),
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Пароль",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = LightGrey
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(27.dp),
            enabled = !isLoading
        )

        errorMessage?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = it,
                color = Color.Red,
                fontSize = 14.sp
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        AuthButton(
            text = "Регистрация",
            onClick = {
                println("DEBUG: Registration button clicked! Name: $name, Password: $password")
                onRegisterClick(name, password)
            },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoading && name.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.height(30.dp))

        AuthLinkText(
            mainText = "Уже есть профиль? ",
            linkText = "Войти",
            onLinkClick = onLoginClick,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun RegPreview() {
}