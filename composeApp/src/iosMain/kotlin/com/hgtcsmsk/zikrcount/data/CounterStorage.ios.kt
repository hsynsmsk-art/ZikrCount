package com.hgtcsmsk.zikrcount.data

import com.russhwolf.settings.NSUserDefaultsSettings
import com.russhwolf.settings.Settings
import platform.Foundation.NSUserDefaults

// commonMain'deki 'expect' fonksiyonunun iOS için 'actual' karşılığı
actual fun createSettings(): Settings {
    // iOS'in kendi ayar saklama mekanizması olan NSUserDefaults'u kullanır.
    val delegate = NSUserDefaults.standardUserDefaults
    return NSUserDefaultsSettings(delegate)
}