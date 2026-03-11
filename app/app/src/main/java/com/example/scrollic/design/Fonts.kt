package com.example.scrollic.design

import androidx.compose.runtime.Composable
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import com.example.scrollic.R

enum class InterFontType {
    BOLD, EXTRA_BOLD, EXTRA_LIGHT, LIGHT, MEDIUM, REGULAR, SEMI_BOLD, THIN
}

@Composable
fun getInterFont(fontName: InterFontType) : FontFamily {
    return when(fontName) {
        InterFontType.BOLD -> FontFamily(Font(R.font.inter_bold))
        InterFontType.EXTRA_BOLD -> FontFamily(Font(R.font.inter_extrabold))
        InterFontType.EXTRA_LIGHT -> FontFamily(Font(R.font.inter_extralight))
        InterFontType.LIGHT -> FontFamily(Font(R.font.inter_light))
        InterFontType.MEDIUM -> FontFamily(Font(R.font.inter_medium))
        InterFontType.REGULAR -> FontFamily(Font(R.font.inter_regular))
        InterFontType.SEMI_BOLD -> FontFamily(Font(R.font.inter_semibold))
        InterFontType.THIN -> FontFamily(Font(R.font.inter_thin))
    }
}