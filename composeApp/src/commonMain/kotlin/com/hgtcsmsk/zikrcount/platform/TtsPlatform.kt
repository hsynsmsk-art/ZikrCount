package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Immutable

@Immutable
data class TtsEngineInfo(
    val name: String, // Paket adı (kod için)
    val label: String // Kullanıcıya gösterilecek etiket
)

expect fun getTtsEngines(): List<TtsEngineInfo>