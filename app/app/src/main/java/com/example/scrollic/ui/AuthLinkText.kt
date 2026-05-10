package com.example.scrollic.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import com.example.scrollic.design.InterFontType
import com.example.scrollic.design.LightGrey
import com.example.scrollic.design.Pink
import com.example.scrollic.design.getInterFont

@Composable
fun AuthLinkText(
    mainText: String,
    linkText: String,
    onLinkClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = mainText,
            fontFamily = getInterFont(InterFontType.REGULAR),
            fontSize = 20.sp,
            color = LightGrey
        )

        Text(
            text = linkText,
            fontFamily = getInterFont(InterFontType.SEMI_BOLD),
            fontSize = 20.sp,
            color = Pink,
            modifier = Modifier.clickable(enabled = enabled) { onLinkClick() }
        )
    }
}