package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import com.hgtcsmsk.zikrcount.platform.SoundPlayer
import platform.AVFoundation.AVAudioPlayer
import platform.AVFoundation.AVAudioSession
import platform.AVFoundation.AVAudioSessionCategoryPlayback
import platform.AVFoundation.setActive
import platform.AVFoundation.setCategory
import platform.Foundation.NSBundle

actual class SoundPlayer {
    private val audioPlayers = mutableMapOf<String, AVAudioPlayer>()

    init {
        setupAudioSession()
        loadAllSounds() // İYİLEŞTİRME: Karmaşık mantığı kendi fonksiyonuna taşıdık.
    }

    private fun setupAudioSession() {
        try {
            AVAudioSession.sharedInstance().setCategory(AVAudioSessionCategoryPlayback, null)
            AVAudioSession.sharedInstance().setActive(true, null)
        } catch (e: Exception) {
            println("AVAudioSession ayarlanırken hata oluştu: ${e.message}")
        }
    }

    private fun loadAllSounds() {
        val soundFiles = mapOf("audio_click" to "mp3", "mini_click" to "mp3", "target" to "mp3")

        soundFiles.forEach { (name, type) ->
            val soundUrl = NSBundle.mainBundle.URLForResource(name, type)
            if (soundUrl != null) {
                // AVAudioPlayer oluşturulurken hata kontrolü için errorPtr null geçilebilir.
                // Eğer player null değilse, başarıyla oluşturulmuş demektir.
                val player = AVAudioPlayer(soundUrl, errorPtr = null)
                if (player != null) {
                    player.prepareToPlay()
                    audioPlayers[name] = player
                } else {
                    println("AVAudioPlayer oluşturulamadı: $name.$type")
                }
            } else {
                println("Ses dosyası bulunamadı: $name.$type")
            }
        }
    }

    actual fun play(soundName: String) {
        audioPlayers[soundName]?.apply {
            setCurrentTime(0.0) // Sesi her zaman başa sar
            play()
        }
    }

    // DÜZELTME: 'actual' anahtar kelimesi eklendi.
    actual fun release() {
        audioPlayers.values.forEach { it.stop() }
        audioPlayers.clear()
        println("SoundPlayer kaynakları temizlendi.")
    }
}

@Composable
actual fun rememberSoundPlayer(): SoundPlayer {
    val soundPlayer = remember { SoundPlayer() }

    DisposableEffect(Unit) {
        onDispose {
            soundPlayer.release()
        }
    }

    return soundPlayer
}