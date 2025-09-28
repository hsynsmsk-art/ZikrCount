package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform

// --- YENİ EKLENDİ ---
/**
 * Uygulamanın paketinden mevcut sürüm kodunu (versionCode) alır.
 * Android'de build.gradle'dan, iOS'ta Info.plist'ten okunur.
 */
expect fun getAppVersionCode(): Int
// --- YENİ EKLENEN KISIM SONU ---

@Composable
expect fun SystemBackButtonHandler(onBackPressed: () -> Unit)

interface PlatformActionHandler {
    fun showShareSheet()
    fun openAppStore()
    fun performCustomVibration()
    fun launchInAppReview()
    fun openTtsSettings()
    fun openUrl(url: String)
    fun sendEmail(address: String, subject: String)
}

@Composable
expect fun rememberPlatformActionHandler(): PlatformActionHandler

expect fun setBackupEnabled(isEnabled: Boolean)

expect fun isInternetAvailable(): Boolean