package com.hgtcsmsk.zikrcount.platform

import android.app.backup.BackupAgentHelper
import android.app.backup.SharedPreferencesBackupHelper
import com.hgtcsmsk.zikrcount.data.CounterStorage
import com.hgtcsmsk.zikrcount.data.createSettings

class ZikrBackupAgent : BackupAgentHelper() {

    private val prefsFileName = "zikr_settings"

    override fun onCreate() {
        val settings = createSettings()
        val storage = CounterStorage(settings)
        val isBackupEnabled = storage.getBackupSetting()

        if (isBackupEnabled) {
            val backupHelper = SharedPreferencesBackupHelper(this, prefsFileName)
            addHelper("zikr_prefs_backup", backupHelper)
        }
    }
}