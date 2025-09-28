package com.hgtcsmsk.zikrcount.platform

import android.app.Activity
import android.app.backup.BackupManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.google.android.play.core.review.ReviewManagerFactory
import com.hgtcsmsk.zikrcount.data.CounterStorage
import com.hgtcsmsk.zikrcount.data.appContext
import com.hgtcsmsk.zikrcount.data.createSettings

class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.SDK_INT}"
}

actual fun getPlatform(): Platform = AndroidPlatform()

// --- YENİ EKLENDİ ---
actual fun getAppVersionCode(): Int {
    return try {
        val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        packageInfo.versionCode
    } catch (e: Exception) {
        // Hata durumunda varsayılan olarak düşük bir değer dön,
        // böylece güncelleme kontrolü güvenli tarafta kalır.
        1
    }
}
// --- YENİ EKLENEN KISIM SONU ---


@Composable
actual fun SystemBackButtonHandler(onBackPressed: () -> Unit) {
    BackHandler(onBack = onBackPressed)
}

@Composable
actual fun rememberPlatformActionHandler(): PlatformActionHandler {
    val context = LocalContext.current
    val activity = context as? Activity
    val reviewManager = remember { ReviewManagerFactory.create(appContext) }

    return remember {
        object : PlatformActionHandler {
            override fun showShareSheet() {
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, "Bu harika zikirmatik uygulamasını mutlaka deneyin! https://play.google.com/store/apps/details?id=${appContext.packageName}")
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, null).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                appContext.startActivity(shareIntent)
            }

            override fun openAppStore() {
                val intent = Intent(Intent.ACTION_VIEW).apply {
                    data = android.net.Uri.parse("market://details?id=${appContext.packageName}")
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    appContext.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    val fallbackIntent = Intent(Intent.ACTION_VIEW).apply {
                        data = android.net.Uri.parse("https://play.google.com/store/apps/details?id=${appContext.packageName}")
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(fallbackIntent)
                }
            }

            override fun performCustomVibration() {
                val vibrator = appContext.getSystemService(Vibrator::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    vibrator.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_TICK))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(10)
                }
            }

            override fun launchInAppReview() {
                if (activity != null) {
                    val request = reviewManager.requestReviewFlow()
                    request.addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            val reviewInfo = task.result
                            val flow = reviewManager.launchReviewFlow(activity, reviewInfo)
                            flow.addOnCompleteListener { _ -> }
                        }
                    }
                }
            }

            override fun openTtsSettings() {
                try {
                    val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(intent)
                } catch (e: ActivityNotFoundException) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        appContext.startActivity(intent)
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                    }
                }
            }

            override fun openUrl(url: String) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url)).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun sendEmail(address: String, subject: String) {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("mailto:") // Sadece e-posta uygulamaları
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(intent)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

actual fun setBackupEnabled(isEnabled: Boolean) {
    val settings = createSettings()
    val storage = CounterStorage(settings)
    storage.saveBackupSetting(isEnabled)
    BackupManager(appContext).dataChanged()
}

actual fun isInternetAvailable(): Boolean {
    val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    val network = connectivityManager.activeNetwork ?: return false
    val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
    return when {
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
        activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
        else -> false
    }
}