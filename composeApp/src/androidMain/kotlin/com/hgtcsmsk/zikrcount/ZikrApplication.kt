package com.hgtcsmsk.zikrcount

import android.app.Application
import com.google.android.gms.ads.MobileAds
import com.hgtcsmsk.zikrcount.data.appContext
import com.hgtcsmsk.zikrcount.platform.TtsManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ZikrApplication : Application() {

    private val applicationScope = CoroutineScope(Dispatchers.IO)

    override fun onCreate() {
        super.onCreate()
        appContext = this
        TtsManager.initialize(this) // TtsManager'ı başlat

        delayedInit()
    }

    private fun delayedInit() {
        applicationScope.launch {
            MobileAds.initialize(this@ZikrApplication)
        }
    }
}