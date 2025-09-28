package com.hgtcsmsk.zikrcount.platform

import android.speech.tts.TextToSpeech
import com.hgtcsmsk.zikrcount.data.appContext

actual fun getTtsEngines(): List<TtsEngineInfo> {
    var tts: TextToSpeech? = null
    val engines = try {
        tts = TextToSpeech(appContext, null)
        tts.engines.map {
            TtsEngineInfo(name = it.name, label = it.label)
        }
    } catch (e: Exception) {
        e.printStackTrace()
        emptyList()
    } finally {
        tts?.shutdown()
    }
    return engines
}