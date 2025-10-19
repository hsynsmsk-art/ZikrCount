package com.hgtcsmsk.zikrcount.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp


val DefaultTypography = Typography(

    bodySmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300,
        fontSize = 15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 15.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 15.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300,
        fontSize = 16.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 16.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W300,
        fontSize = 14.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.W500,
        fontSize = 14.sp,
    )
)

fun Typography.withAdjustedFontSizes(amount: Int): Typography {
    return this.copy(
        bodyLarge = this.bodyLarge.copy(fontSize = (this.bodyLarge.fontSize.value + amount).sp),
        bodyMedium = this.bodyMedium.copy(fontSize = (this.bodyMedium.fontSize.value + amount).sp),
        bodySmall = this.bodySmall.copy(fontSize = (this.bodySmall.fontSize.value + amount).sp),
        titleLarge = this.titleLarge.copy(fontSize = (this.titleLarge.fontSize.value + amount).sp),
        titleMedium = this.titleMedium.copy(fontSize = (this.titleMedium.fontSize.value + amount).sp),
        titleSmall = this.titleSmall.copy(fontSize = (this.titleSmall.fontSize.value + amount).sp),
        labelLarge = this.labelLarge.copy(fontSize = (this.labelLarge.fontSize.value + amount).sp),
        labelMedium = this.labelMedium.copy(fontSize = (this.labelMedium.fontSize.value + amount).sp),
        labelSmall = this.labelSmall.copy(fontSize = (this.labelSmall.fontSize.value + amount).sp)
    )
}