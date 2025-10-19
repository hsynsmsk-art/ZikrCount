package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable

expect fun getAppVersionCode(): Int

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

expect fun isAccessibilityServiceEnabled(): Boolean