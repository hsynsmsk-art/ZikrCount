package com.hgtcsmsk.zikrcount.platform

import android.content.Context
import android.speech.tts.TextToSpeech
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

actual object TtsManager : TextToSpeech.OnInitListener {
    private const val TAG = "TTS_MANAGER"
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    private lateinit var appContext: Context
    private var currentEngine: String? = null
    private var currentLanguage: String = ""

    private val _availableLanguages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    actual val availableLanguages = _availableLanguages.asStateFlow()

    actual fun initialize(context: Any) {
        if (isInitialized) return
        appContext = (context as Context).applicationContext
    }

    actual fun reconfigure(engine: String?, language: String) {
        currentEngine = engine
        currentLanguage = language
        tts?.stop()
        tts?.shutdown()
        tts = TextToSpeech(appContext, this, engine)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            val locales = tts?.availableLanguages?.mapNotNull { locale ->
                try {
                    // getDisplayName() metodunu parametresiz kullanarak daha açıklayıcı isimler alın.
                    // Örn: "English (United Kingdom)" gibi.
                    val displayName = locale.getDisplayName()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    if (displayName.isNotBlank()) Pair(locale.toLanguageTag(), displayName) else null
                } catch (e: Exception) { null }
                // Tekilleştirmeyi, benzersiz olan dil koduna (it.first) göre yapın.
            }?.distinctBy { it.first }?.sortedBy { it.second } ?: emptyList()
            _availableLanguages.value = locales
            setLanguageInternal()
        } else {
            isInitialized = false
            _availableLanguages.value = emptyList()
            Log.e(TAG, "onInit: HATA! Motor başlatılamadı!")
        }
    }

    private fun setLanguageInternal() {
        if (!isInitialized) return
        if (currentLanguage.isNotEmpty()) {
            val locale = Locale.forLanguageTag(currentLanguage)
            if (isLanguageAvailable(locale)) {
                tts?.language = locale
                return
            }
        }
        tts?.language = Locale.getDefault()
    }

    actual fun speak(text: String, rate: Float) {
        if (isInitialized) {
            tts?.setSpeechRate(rate)
            tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
        }
    }

    private fun isLanguageAvailable(locale: Locale): Boolean {
        return tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED >= TextToSpeech.LANG_AVAILABLE
    }
}