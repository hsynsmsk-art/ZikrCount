package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Immutable

@Immutable
data class TtsEngineInfo(
    val name: String,
    val label: String
)

expect fun getTtsEngines(): List<TtsEngineInfo>

expect fun getLocalizedString(resourceName: String, languageTag: String): String