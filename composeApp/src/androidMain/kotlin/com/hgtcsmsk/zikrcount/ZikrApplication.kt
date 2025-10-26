package com.hgtcsmsk.zikrcount

import android.app.Application
import com.hgtcsmsk.zikrcount.data.appContext
import com.hgtcsmsk.zikrcount.platform.TtsManager

open class ZikrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        appContext = this
        TtsManager.initialize(this)
    }
}