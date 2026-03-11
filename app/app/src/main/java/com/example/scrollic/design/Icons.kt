package com.example.scrollic.design

import android.media.Image
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.vectorResource
import com.example.scrollic.R

object Icons {

    val logo: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.logo)

    val likeEmpty: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.like_empty)

    val likeFull: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.like_full)

    val arrowGray: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.arrow_gray)

    val arrowWhite: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.arrow_white)

    val menu: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.menu_icon)

    val themeBright: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.theme_bright)

    val themeDark: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.theme_dark)

    val themeLight: ImageVector
        @Composable
        get() = ImageVector.vectorResource(id = R.drawable.theme_light)
}

enum class IconType {
    LOGO, LIKE_EMPTY, LIKE_FULL, ARROW_GRAY, ARROW_WHITE, MENU, THEME_BRIGHT, THEME_DARK, THEME_LIGHT
}

@Composable
fun getIcon(iconType: IconType): ImageVector {
    return when(iconType) {
        IconType.LOGO -> Icons.logo
        IconType.LIKE_EMPTY -> Icons.likeEmpty
        IconType.LIKE_FULL -> Icons.likeFull
        IconType.ARROW_GRAY -> Icons.arrowGray
        IconType.ARROW_WHITE -> Icons.arrowWhite
        IconType.MENU -> Icons.menu
        IconType.THEME_BRIGHT -> Icons.themeBright
        IconType.THEME_DARK -> Icons.themeDark
        IconType.THEME_LIGHT -> Icons.themeLight
    }
}