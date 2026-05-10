package com.example.scrollic.screens.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.LightGrey
import com.example.scrollic.design.Pink
import com.example.scrollic.design.White
import com.example.scrollic.design.getInterFont
import com.example.scrollic.ui.AuthButton
import com.example.scrollic.ui.AuthLinkText

@Composable
fun LoginSheetContent(
    isLoading: Boolean,
    errorMessage: String?,
    onLoginClick: (name: String, password: String) -> Unit,
    onRegisterClick: () -> Unit
) {
    var name by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp)
    ) {
        Text(
            text = "Вход",
            fontFamily = getInterFont(InterFontType.SEMI_BOLD),
            fontSize = 30.sp,
            color = Pink
        )

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "Имя пользователя",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = LightGrey
        )

        Spacer(modifier = Modifier.height(10.dp))

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

        Spacer(modifier = Modifier.height(10.dp))

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
                fontSize = 14.sp,
                fontFamily = getInterFont(InterFontType.REGULAR)
            )
        }

        Spacer(modifier = Modifier.height(35.dp))

        AuthButton(
            text = "Вход",
            onClick = { onLoginClick(name, password) },
            modifier = Modifier.align(Alignment.CenterHorizontally),
            enabled = !isLoading && name.isNotBlank() && password.isNotBlank()
        )

        Spacer(modifier = Modifier.height(30.dp))

        AuthLinkText(
            mainText = "Нет профиля? ",
            linkText = "Регистрация",
            onLinkClick = onRegisterClick,
            enabled = !isLoading
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun LogPreview() {

}