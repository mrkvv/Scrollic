package com.example.scrollic.screens.extra

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.scrollic.design.Blue
import com.example.scrollic.design.IconType
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.LightGrey
import com.example.scrollic.design.Pink
import com.example.scrollic.design.White
import com.example.scrollic.design.getIcon
import com.example.scrollic.design.getInterFont

@Composable
fun EditingProfileSheetContent(
   onBackClick: () -> Unit
){
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 40.dp, vertical = 20.dp)
    ) {

        Spacer(modifier = Modifier.height(35.dp))

        Text(
            text = "Имя",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = White
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassInput(
            value = name,
            onValueChange = { name = it }
        )

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = "Email",
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = White
        )

        Spacer(modifier = Modifier.height(10.dp))

        GlassInput(
            value = email,
            onValueChange = { email = it }
        )

        Spacer(modifier = Modifier.height(50.dp))
        
        SaveProfileButton(
            onClick = {},
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(218.dp))

        Row(
            modifier = Modifier
                .clickable { onBackClick() },
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

        Spacer(modifier = Modifier.height(20.dp))
    }
}

@Composable
fun EditGlassSheet(
    content: @Composable ColumnScope.() -> Unit
) {
    val shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(shape)

            .background(
                color = Color(0xFF2A2F3A)
            )
    ) {
        content()
    }
}

@Composable
fun GlassInput(
    value: String,
    onValueChange: (String) -> Unit
) {
    TextField(
        value = value,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .height(55.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(
                Color.White.copy(alpha = 0.9f)
            ),

        colors = TextFieldDefaults.colors(
            focusedContainerColor = Color.Transparent,
            unfocusedContainerColor = Color.Transparent,

            focusedIndicatorColor = Color.Transparent,
            unfocusedIndicatorColor = Color.Transparent,

            cursorColor = Color.Black,
            focusedTextColor = Color.Black,
            unfocusedTextColor = Color.Black
        ),

        singleLine = true,
        shape = RoundedCornerShape(30.dp)
    )
}

@Composable
fun SaveProfileButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(190.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(Blue)
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
            text = "Сохранить",
            fontFamily = getInterFont(InterFontType.BOLD),
            fontSize = 24.sp,
            color = White
        )
    }
}

@Preview(showBackground = true)
@Composable
fun EditProfPreview() {
    EditingProfileSheetContent(
        onBackClick = {},
    )
}