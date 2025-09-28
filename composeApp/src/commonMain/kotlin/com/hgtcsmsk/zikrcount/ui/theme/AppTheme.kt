package com.hgtcsmsk.zikrcount.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

data class AppColors(
    val primary: Color,
    val secondary: Color,
    val background: Color = Color(0xFF3F3F3F),
    val surface: Color = Color(0xFFDDDDDD),
    val textOnPrimary: Color = Color.White,
    val textGeneral: Color = Color.Black
)

val LocalAppColors = staticCompositionLocalOf {
    AppColors(
        primary = Color(0xFFfcc95b),
        secondary = Color(0xFFffda93)
    )
}

object ZikrTheme {
    val colors: AppColors
        @Composable
        get() = LocalAppColors.current
}

@Composable
fun ZikrTheme(
    primaryColor: Color,
    secondaryColor: Color,
    content: @Composable () -> Unit
) {
    val colors = AppColors(
        primary = primaryColor,
        secondary = secondaryColor
    )
    CompositionLocalProvider(LocalAppColors provides colors) {
        content()
    }
}

