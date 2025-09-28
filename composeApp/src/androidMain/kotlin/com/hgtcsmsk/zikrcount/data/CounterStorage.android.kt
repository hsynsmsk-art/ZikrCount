package com.hgtcsmsk.zikrcount.data

import android.content.Context
import com.russhwolf.settings.Settings
import com.russhwolf.settings.SharedPreferencesSettings


lateinit var appContext: Context

actual fun createSettings(): Settings {
    return SharedPreferencesSettings(appContext.getSharedPreferences("zikr_settings", Context.MODE_PRIVATE))
}