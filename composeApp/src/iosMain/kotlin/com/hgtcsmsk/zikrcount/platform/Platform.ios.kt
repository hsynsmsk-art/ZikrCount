package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable
import com.hgtcsmsk.zikrcount.data.createSettings
import platform.Foundation.NSDocumentDirectory
import platform.Foundation.NSUserDomainMask
import platform.UIKit.UIDevice
import com.hgtcsmsk.zikrcount.data.CounterStorage
import kotlinx.cinterop.ExperimentalForeignApi
import platform.Foundation.NSFileManager
import platform.Foundation.NSURLIsExcludedFromBackupKey

class IOSPlatform: Platform {
    override val name: String = UIDevice.currentDevice.systemName() + " " + UIDevice.currentDevice.systemVersion
}

actual fun getPlatform(): Platform = IOSPlatform()

@Composable
actual fun SystemBackButtonHandler(onBackPressed: () -> Unit) {
    // iOS'ta genel bir sistem geri tuşu olmadığı için bu genellikle boştur.
    // Navigasyon, genellikle UI içindeki geri butonları ile yönetilir.
}

@OptIn(ExperimentalForeignApi::class)
actual fun setBackupEnabled(isEnabled: Boolean) {
    val settings = createSettings()
    val storage = CounterStorage(settings)
    storage.saveBackupSetting(isEnabled)

    try {
        val fileManager = NSFileManager.defaultManager
        val documentsDirectory = fileManager.URLForDirectory(
            directory = NSDocumentDirectory,
            inDomain = NSUserDomainMask,
            appropriateForURL = null,
            create = false,
            error = null
        )

        val fileName = "zikr_settings.preferences_pb"
        val settingsFileURL = documentsDirectory?.URLByAppendingPathComponent(fileName)

        if (settingsFileURL != null) {
            val key = NSURLIsExcludedFromBackupKey
            val value = !isEnabled // Yedekleme AÇIK ise, muafiyet KAPALI (false) olmalıdır.
            settingsFileURL.setResourceValue(value, forKey = key, error = null)
        }
    } catch (e: Exception) {
        println("iCloud yedekleme ayarı değiştirilemedi: ${e.message}")
    }
}