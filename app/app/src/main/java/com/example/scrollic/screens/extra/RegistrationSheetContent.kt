package com.example.scrollic.screens.extra

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

@Composable
fun RegistrationSheetContent(
    onRegisterClick: () -> Unit,
    onLoginClick: () -> Unit
) {

    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
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

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(27.dp)
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Email",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = LightGrey
        )

        Spacer(modifier = Modifier.height(10.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(27.dp)
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
            shape = RoundedCornerShape(27.dp)
        )

        Spacer(modifier = Modifier.height(35.dp))

        RegistrationButton(
            onClick = onRegisterClick,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(30.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Center
        ){

            Text(
                text = "Уже есть профиль? ",
                fontFamily = getInterFont(InterFontType.REGULAR),
                fontSize = 20.sp,
                color = LightGrey
            )

            Text(
                text = "Войти",
                fontFamily = getInterFont(InterFontType.SEMI_BOLD),
                fontSize = 20.sp,
                color = Pink,
                modifier = Modifier.clickable { onLoginClick() }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun RegistrationButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(190.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Pink)
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
                    color = Color(0xFF737373),
                    alpha = 0.25f
                )
            )
            .clickable { onClick() },

        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "Регистрация",
            fontFamily = getInterFont(InterFontType.BOLD),
            fontSize = 24.sp,
            color = White
        )
    }
}

@Composable
fun GlassSheet(
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.White.copy(alpha = 0.75f),
                        Color.White.copy(alpha = 1f)
                    )
                )
            )
            .innerShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 1.5.dp,
                    spread = 0.dp,
                    offset = DpOffset(2.dp, 2.dp),
                    color = Color.White,
                    alpha = 0.4f
                )
            )

            .innerShadow(
                shape = shape,
                shadow = Shadow(
                    radius = 6.dp,
                    spread = 0.dp,
                    offset = DpOffset((-3).dp, (-3).dp),
                    color = Color.White,
                    alpha = 0.25f
                )
            )
    ) {
        content()
    }
}

@Preview(showBackground = true)
@Composable
fun RegPreview() {
    RegistrationSheetContent(
        onRegisterClick = {},
        onLoginClick = {}
    )
}