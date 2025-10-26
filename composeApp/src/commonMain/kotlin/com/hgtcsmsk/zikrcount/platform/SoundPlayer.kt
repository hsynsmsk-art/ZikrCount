package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable

@Composable
expect fun rememberSoundPlayer(): SoundPlayer

@Suppress("EXPECT_ACTUAL_CLASSIFIERS_ARE_IN_BETA_WARNING")
expect class SoundPlayer {
    fun play(soundName: String, volume: Float = 1.0f)
    fun release()
}