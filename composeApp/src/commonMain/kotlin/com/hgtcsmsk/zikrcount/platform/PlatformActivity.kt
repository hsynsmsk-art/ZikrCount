package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable

/**
 * Bu Composable fonksiyon, o anki platformun "Activity" (Android) veya
 * "UIViewController" (iOS) gibi ana ekran bileşenini almamızı sağlar.
 * Bu, ortak koddan platforma özel bir referans almanın doğru yoludur.
 */
@Composable
expect fun rememberPlatformActivity(): Any