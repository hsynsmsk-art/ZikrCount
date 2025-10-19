package com.hgtcsmsk.zikrcount.platform

import android.app.Activity
import android.app.backup.BackupManager
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.view.accessibility.AccessibilityManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.pm.PackageInfoCompat
import androidx.core.net.toUri
import com.google.android.play.core.review.ReviewManagerFactory
import com.hgtcsmsk.zikrcount.R
import com.hgtcsmsk.zikrcount.data.CounterStorage
import com.hgtcsmsk.zikrcount.data.appContext
import com.hgtcsmsk.zikrcount.data.createSettings
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

actual fun getAppVersionCode(): Int {
    return try {
        val packageInfo = appContext.packageManager.getPackageInfo(appContext.packageName, 0)
        PackageInfoCompat.getLongVersionCode(packageInfo).toInt()
    } catch (e: Exception) { e.printStackTrace(); 1}
}

@Composable
actual fun SystemBackButtonHandler(onBackPressed: () -> Unit) {
    BackHandler(onBack = onBackPressed)
}

@Composable
actual fun rememberPlatformActionHandler(): PlatformActionHandler {
    val context = LocalContext.current
    val activity = context as? Activity
    val reviewManager = remember { ReviewManagerFactory.create(appContext) }
    val shareTextFormat = stringResource(Res.string.share_text)
    val shareTitle = stringResource(Res.string.share_title)
    val contactEmailAddress = stringResource(Res.string.contact_email_address)
    val contactEmailSubject = stringResource(Res.string.contact_email_subject)

    return remember {
        object : PlatformActionHandler {
            override fun showShareSheet() {
                val shareText = String.format(shareTextFormat, appContext.packageName)
                val sendIntent: Intent = Intent().apply {
                    action = Intent.ACTION_SEND
                    putExtra(Intent.EXTRA_TEXT, shareText)
                    type = "text/plain"
                }
                val shareIntent = Intent.createChooser(sendIntent, shareTitle)
                if (shareIntent.resolveActivity(appContext.packageManager) != null) {
                    appContext.startActivity(shareIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK))
                } else {
                    Toast.makeText(appContext, appContext.getString(R.string.error_no_share_app), Toast.LENGTH_SHORT).show()
                }
            }

            override fun openAppStore() {
                val marketUri = "market://details?id=${appContext.packageName}".toUri()
                val intent = Intent(Intent.ACTION_VIEW, marketUri).apply {
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                try {
                    appContext.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    val webUri = "https://play.google.com/store/apps/details?id=${appContext.packageName}".toUri()
                    val fallbackIntent = Intent(Intent.ACTION_VIEW, webUri).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (fallbackIntent.resolveActivity(appContext.packageManager) != null) {
                        appContext.startActivity(fallbackIntent)
                    } else {
                        Toast.makeText(appContext, appContext.getString(R.string.error_no_store_or_browser), Toast.LENGTH_SHORT).show()
                    }
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
                            flow.addOnCompleteListener { _ ->
                            }
                        } else { openAppStore() }
                    }
                } else { openAppStore() }
            }

            override fun openTtsSettings() {
                try {
                    val intent = Intent("com.android.settings.TTS_SETTINGS").apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    appContext.startActivity(intent)
                } catch (_: ActivityNotFoundException) {
                    try {
                        val intent = Intent(android.provider.Settings.ACTION_SETTINGS).apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        appContext.startActivity(intent)
                    } catch (e2: Exception) {
                        e2.printStackTrace()
                        Toast.makeText(appContext, appContext.getString(R.string.error_cannot_open_settings), Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun openUrl(url: String) {
                try {
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri()).apply {
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(appContext.packageManager) != null) {
                        appContext.startActivity(intent)
                    } else {
                        Toast.makeText(appContext, appContext.getString(R.string.error_cannot_open_link_app), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(appContext, appContext.getString(R.string.error_cannot_open_link), Toast.LENGTH_SHORT).show()
                }
            }

            override fun sendEmail(address: String, subject: String) {
                try {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:".toUri()
                        putExtra(Intent.EXTRA_EMAIL, arrayOf(address))
                        putExtra(Intent.EXTRA_SUBJECT, subject)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }
                    if (intent.resolveActivity(appContext.packageManager) != null) {
                        appContext.startActivity(intent)
                    } else {
                        Toast.makeText(appContext, appContext.getString(R.string.error_no_email_app), Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    Toast.makeText(appContext, appContext.getString(R.string.error_cannot_send_email), Toast.LENGTH_SHORT).show()
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

actual fun isAccessibilityServiceEnabled(): Boolean {
    val accessibilityManager = appContext.getSystemService(Context.ACCESSIBILITY_SERVICE) as? AccessibilityManager
    return accessibilityManager?.isEnabled == true && accessibilityManager.isTouchExplorationEnabled
}