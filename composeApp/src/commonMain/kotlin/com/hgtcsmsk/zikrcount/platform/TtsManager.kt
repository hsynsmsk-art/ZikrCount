package com.hgtcsmsk.zikrcount.platform

import kotlinx.coroutines.flow.StateFlow

expect object TtsManager {
    val availableLanguages: StateFlow<List<Pair<String, String>>>
    fun initialize(context: Any)
    fun reconfigure(engine: String?, language: String)
    fun speak(text: String, rate: Float)
}