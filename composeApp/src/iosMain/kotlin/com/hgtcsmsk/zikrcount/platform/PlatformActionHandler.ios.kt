package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import platform.UIKit.UIApplication
import platform.UIKit.UIActivityViewController
import platform.Foundation.NSURL
import platform.UIKit.UIImpactFeedbackGenerator
import platform.UIKit.UIImpactFeedbackStyle
// Artık UIWindow veya UIWindowScene importuna gerek yok

private class IOSPlatformActionHandler : PlatformActionHandler {
    override fun showShareSheet(message: String) {
        val activityViewController = UIActivityViewController(
            activityItems = listOf(message),
            applicationActivities = null
        )

        // --- DEĞİŞEN KISIM ---
        // Hem eski hem de yeni iOS versiyonlarıyla uyumlu, en basit yöntem.
        val window = UIApplication.sharedApplication.keyWindow
        val rootViewController = window?.rootViewController

        rootViewController?.presentViewController(
            viewControllerToPresent = activityViewController,
            animated = true,
            completion = null
        )
        // --- DEĞİŞEN KISIM SONU ---
    }

    override fun openAppStore() {
        // UYGULAMANIZI APP STORE'A YÜKLEDİKTEN SONRA BURADAKİ "YOUR_APP_ID_HERE" KISMINI DEĞİŞTİRİN
        val url = NSURL.URLWithString("https://apps.apple.com/app/idYOUR_APP_ID_HERE")!!
        if (UIApplication.sharedApplication.canOpenURL(url)) {
            UIApplication.sharedApplication.openURL(url)
        }
    }

    override fun performCustomVibration() {
        // ".heavy" stili, daha belirgin ve "uzun" hissedilen bir geri bildirim sağlar.
        val generator = UIImpactFeedbackGenerator(style = UIImpactFeedbackStyle.UIImpactFeedbackStyleHeavy)
        generator.prepare()
        generator.impactOccurred()
    }
}

@Composable
actual fun rememberPlatformActionHandler(): PlatformActionHandler {
    return remember { IOSPlatformActionHandler() }
}