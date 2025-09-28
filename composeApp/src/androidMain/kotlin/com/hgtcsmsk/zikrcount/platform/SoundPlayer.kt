package com.hgtcsmsk.zikrcount.platform

import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.hgtcsmsk.zikrcount.R // DEĞİŞİKLİK: Android'in R sınıfı import edildi.
import com.hgtcsmsk.zikrcount.data.appContext

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val soundPlayer = remember {
        SoundPlayer().apply { loadSounds() }
    }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }
    return soundPlayer
}

actual class SoundPlayer {
    private var soundPool: SoundPool? = null
    private val soundMap = mutableMapOf<String, Int>()
    private val loadedSounds = mutableSetOf<Int>()

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()
        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(5)
            .build()
        soundPool?.setOnLoadCompleteListener { _, sampleId, status ->
            if (status == 0) {
                loadedSounds.add(sampleId)
            }
        }
    }

    fun loadSounds() {
        // DEĞİŞİKLİK: Kaynaklar artık 'Res' yerine Android'in yerel 'R' sınıfından çağrılıyor.
        soundMap["audio_click"] = soundPool?.load(appContext, R.raw.audio_click, 1) ?: 0
        soundMap["mini_click"] = soundPool?.load(appContext, R.raw.mini_click, 1) ?: 0
        soundMap["target"] = soundPool?.load(appContext, R.raw.target, 1) ?: 0
    }

    actual fun play(soundName: String, volume: Float) {
        val soundId = soundMap[soundName]
        if (soundId != null && loadedSounds.contains(soundId)) {
            soundPool?.play(soundId, volume, volume, 1, 0, 1.0f)
        }
    }

    actual fun release() {
        soundPool?.release()
        soundPool = null
    }
}