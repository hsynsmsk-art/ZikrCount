package com.hgtcsmsk.zikrcount.platform

import android.content.Context
import android.media.AudioManager
import android.os.Build
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
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
                    val displayName = locale.getDisplayName()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
                    if (displayName.isNotBlank()) Pair(locale.toLanguageTag(), displayName) else null
                } catch (_: Exception) {
                    null
                }
            }?.distinctBy { it.first }?.sortedBy { it.second } ?: emptyList()
            _availableLanguages.value = locales
            setLanguageInternal()
        } else {
            isInitialized = false
            _availableLanguages.value = emptyList()
            Log.e(TAG, "onInit: ERROR! TTS Engine initialization failed!")
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
        tts?.language = try {
            val defaultLocale = tts?.defaultVoice?.locale
            defaultLocale ?: Locale.getDefault()
        } catch (_: Exception) {
            Locale.ENGLISH
        }
    }


    actual fun speak(text: String, rate: Float) {
        if (isInitialized && ::appContext.isInitialized) {
            tts?.setSpeechRate(rate)

            val audioManager = appContext.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            if (audioManager == null) {
                Log.e(TAG, "AudioManager could not be obtained.")
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, "ZikrCountUtteranceId_NoAudioMgr")
                return
            }

            val utteranceId = "ZikrCountUtteranceId"
            var originalMediaVolume = -1
            try {
                originalMediaVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to get original media volume.", e)
            }
            val targetVolume = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                try {
                    audioManager.getStreamVolume(AudioManager.STREAM_ACCESSIBILITY)
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to get accessibility volume, using original.", e)
                    originalMediaVolume
                }
            } else {
                originalMediaVolume
            }

            val listener = object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {}

                override fun onDone(utteranceId: String?) {
                    if (originalMediaVolume != -1) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to restore volume onDone.", e)
                        }
                    }
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    if (originalMediaVolume != -1) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to restore volume onError (deprecated).", e)
                        }
                    }
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    if (originalMediaVolume != -1) {
                        try {
                            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to restore volume onError.", e)
                        }
                    }
                    Log.e(TAG, "TTS Error: Code $errorCode")
                }
            }
            tts?.setOnUtteranceProgressListener(listener)

            try {
                if (targetVolume >= 0 && targetVolume != originalMediaVolume) {
                    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, targetVolume, 0)
                }
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)

            } catch (e: SecurityException) {
                Log.e(TAG, "Failed to change volume. Do Not Disturb mode might be active.", e)
                tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, utteranceId)
            } catch (e: Exception) {
                Log.e(TAG, "Failed to speak.", e)
                if (originalMediaVolume != -1) {
                    try {
                        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, originalMediaVolume, 0)
                    } catch (eRestore: Exception) {
                        Log.e(TAG, "Failed to restore volume after speak error.", eRestore)
                    }
                }
            }
        } else {
            Log.w(TAG, "TTS not initialized or context not available, cannot speak.")
        }
    }

    private fun isLanguageAvailable(locale: Locale): Boolean {
        val result = (tts?.isLanguageAvailable(locale) ?: TextToSpeech.LANG_NOT_SUPPORTED)
        return result == TextToSpeech.LANG_AVAILABLE ||
                result == TextToSpeech.LANG_COUNTRY_AVAILABLE ||
                result == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE
    }

}