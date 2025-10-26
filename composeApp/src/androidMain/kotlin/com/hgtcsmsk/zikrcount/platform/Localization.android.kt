package com.hgtcsmsk.zikrcount.platform

import com.hgtcsmsk.zikrcount.data.appContext
import java.util.Locale

actual fun getLocalizedString(resourceName: String, languageTag: String): String {
    return try {
        val ttsLocale = Locale.forLanguageTag(languageTag)
        val config = android.content.res.Configuration(appContext.resources.configuration)
        config.setLocale(ttsLocale)
        val localizedContext = appContext.createConfigurationContext(config)

        val resId = localizedContext.resources.getIdentifier(
            resourceName,
            "string",
            appContext.packageName
        )

        if (resId == 0) {
            return appContext.getString(
                appContext.resources.getIdentifier(
                    resourceName,
                    "string",
                    appContext.packageName
                )
            )
        }
        localizedContext.resources.getString(resId)
    } catch (e: Exception) {
        e.printStackTrace()
        try {
            appContext.getString(
                appContext.resources.getIdentifier(
                    resourceName,
                    "string",
                    appContext.packageName
                )
            )
        } catch (e2: Exception) {
            ""
        }
    }
}