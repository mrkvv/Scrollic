package com.example.scrollic.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.Pink
import com.example.scrollic.design.White
import com.example.scrollic.design.getInterFont

@Composable
fun AuthButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Box(
        modifier = modifier
            .width(190.dp)
            .height(60.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(if (enabled) Pink else Pink.copy(alpha = 0.5f))
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
            .clickable(enabled = enabled) { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontFamily = getInterFont(InterFontType.BOLD),
            fontSize = 24.sp,
            color = if (enabled) White else White.copy(alpha = 0.6f)
        )
    }
}