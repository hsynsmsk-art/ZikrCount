package com.hgtcsmsk.zikrcount

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.ui.window.ComposeUIViewController
import com.hgtcsmsk.zikrcount.App

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class) // Anotasyonu ekliyoruz
fun MainViewController() = ComposeUIViewController {
    // 1. Tıpkı Android'de olduğu gibi, değeri platform katmanında hesaplıyoruz.
    // iOS'ta bu fonksiyon 'activity' gibi bir parametreye ihtiyaç duymaz.
    val windowSizeClass = calculateWindowSizeClass()

    // 2. Hesaplanan değeri ortak koddaki App fonksiyonuna parametre olarak geçiyoruz.
    App(windowSizeClass = windowSizeClass)
}