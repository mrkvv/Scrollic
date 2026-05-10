package com.example.scrollic.screens.extra

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.innerShadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.shadow.Shadow
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp

@Composable
fun BottomSheet(
    visible: Boolean,
    onDismiss: () -> Unit,
    content: @Composable ColumnScope.() -> Unit
) {
    val screenHeightDp = LocalConfiguration.current.screenHeightDp.dp
    val density = LocalDensity.current

    val screenHeightPx = with(density) { screenHeightDp.roundToPx() }

    val offsetY by animateIntOffsetAsState(
        targetValue = if (visible) {
            IntOffset(0, 0)
        } else {
            IntOffset(0, screenHeightPx)
        },
        animationSpec = tween(
            durationMillis = 320,
            easing = FastOutSlowInEasing
        ),
        label = "sheet_anim"
    )

    val scrimAlpha by animateFloatAsState(
        targetValue = if (visible) 0.3f else 0f,
        animationSpec = tween(300),
        label = "scrim"
    )

    val blurRadius by animateFloatAsState(
        targetValue = if (visible) 12f else 0f,
        animationSpec = tween(300),
        label = "blur"
    )

    Box(
        modifier = Modifier.fillMaxSize()
    ) {
        // Размытый затемняющий фон
        if (scrimAlpha > 0f) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = scrimAlpha))
                    .blur(blurRadius.dp)
                    .pointerInput(Unit) {
                        detectTapGestures {
                            onDismiss()
                        }
                    }
            )
        }

        // BottomSheet с эффектом стекла как в GlassButton
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.BottomCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .offset { offsetY }
                    .clip(RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp))
                    .background(
                        brush = Brush.verticalGradient(
                            colors = listOf(
                                Color.White.copy(alpha = 0.1f),   // Верх прозрачный
                                Color.White.copy(alpha = 0.3f),
                                Color.White.copy(alpha = 0.6f),
                                Color.White                       // Низ белый
                            ),
                            startY = 0f,
                            endY = Float.POSITIVE_INFINITY
                        )
                    )
                    // Внутренние тени как у GlassButton
                    .innerShadow(
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                        shadow = Shadow(
                            radius = 1.3.dp,
                            spread = 0.dp,
                            offset = DpOffset(1.dp, 2.dp),
                            color = Color.White,
                            alpha = 0.41f
                        )
                    )
                    .innerShadow(
                        shape = RoundedCornerShape(topStart = 30.dp, topEnd = 30.dp),
                        shadow = Shadow(
                            radius = 4.dp,
                            spread = 0.dp,
                            offset = DpOffset((-2).dp, (-2).dp),
                            color = Color.White,
                            alpha = 0.25f
                        )
                    )
                    .pointerInput(Unit) {
                        detectTapGestures(
                            onTap = {}
                        )
                    }
            ) {
                content()
            }
        }
    }
}