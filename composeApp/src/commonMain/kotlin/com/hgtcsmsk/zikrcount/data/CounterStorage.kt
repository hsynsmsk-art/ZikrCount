package com.hgtcsmsk.zikrcount.data

import com.russhwolf.settings.Settings
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.builtins.SetSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json


expect fun createSettings(): Settings

class CounterStorage(private val settings: Settings) {

    private val counterListSerializer = ListSerializer(Counter.serializer())
    private val json = Json { ignoreUnknownKeys = true }

    fun saveCounters(counters: List<Counter>) {
        val jsonString = json.encodeToString(counterListSerializer, counters)
        settings.putString(COUNTERS_KEY, jsonString)
    }

    fun getCounters(): List<Counter> {
        val jsonString = settings.getString(COUNTERS_KEY, "[]")
        return try {
            json.decodeFromString(counterListSerializer, jsonString)
        } catch (e: Exception) {
            println("Error reading counters: ${e.message}")
            emptyList()
        }
    }

    fun saveLastUpdateCheckTimestamp(timestamp: Long) {
        settings.putLong(LAST_UPDATE_CHECK_TIMESTAMP_KEY, timestamp)
    }

    fun getLastUpdateCheckTimestamp(): Long {
        return settings.getLong(LAST_UPDATE_CHECK_TIMESTAMP_KEY, 0L)
    }

    fun saveLastSelectedCounterId(id: Long) {
        settings.putLong(LAST_SELECTED_ID_KEY, id)
    }

    fun getLastSelectedCounterId(): Long {
        return settings.getLong(LAST_SELECTED_ID_KEY, 0L)
    }

    fun saveSoundSetting(isEnabled: Boolean) {
        settings.putBoolean(SOUND_SETTING_KEY, isEnabled)
    }

    fun getSoundSetting(): Boolean {
        return settings.getBoolean(SOUND_SETTING_KEY, true)
    }

    fun saveVibrationSetting(isEnabled: Boolean) {
        settings.putBoolean(VIBRATION_SETTING_KEY, isEnabled)
    }

    fun getVibrationSetting(): Boolean {
        return settings.getBoolean(VIBRATION_SETTING_KEY, true)
    }

    fun saveNightModeSetting(isEnabled: Boolean) {
        settings.putBoolean(NIGHT_MODE_SETTING_KEY, isEnabled)
    }

    fun getNightModeSetting(): Boolean {
        return settings.getBoolean(NIGHT_MODE_SETTING_KEY, false)
    }

    fun saveBackgroundSetting(backgroundName: String) {
        settings.putString(BACKGROUND_SETTING_KEY, backgroundName)
    }

    fun getBackgroundSetting(): String {
        return settings.getString(BACKGROUND_SETTING_KEY, "background_1")
    }

    fun saveDisplayThemeSetting(themeName: String) {
        settings.putString(DISPLAY_THEME_SETTING_KEY, themeName)
    }

    fun getDisplayThemeSetting(): String {
        return settings.getString(DISPLAY_THEME_SETTING_KEY, "blueTurquoise")
    }

    fun saveFullScreenTouchSetting(isEnabled: Boolean) {
        settings.putBoolean(FULL_SCREEN_TOUCH_KEY, isEnabled)
    }

    fun getIsFullScreenTouchEnabled(): Boolean {
        return settings.getBoolean(FULL_SCREEN_TOUCH_KEY, false)
    }

    fun saveBackupSetting(isEnabled: Boolean) {
        settings.putBoolean(BACKUP_SETTING_KEY, isEnabled)
    }

    fun getBackupSetting(): Boolean {
        return settings.getBoolean(BACKUP_SETTING_KEY, false)
    }

    fun saveUnlockedItems(items: Set<String>) {
        val jsonString = json.encodeToString(SetSerializer(String.serializer()), items)
        settings.putString(UNLOCKED_ITEMS_KEY, jsonString)
    }

    fun getUnlockedItems(): Set<String> {
        val jsonString = settings.getString(UNLOCKED_ITEMS_KEY, "[]")
        return try {
            json.decodeFromString(SetSerializer(String.serializer()), jsonString)
        } catch (e: Exception) {
            println("Error reading unlocked items: ${e.message}")
            emptySet()
        }
    }

    fun saveFreeSlotsUsed(count: Int) {
        settings.putInt(FREE_SLOTS_USED_KEY, count)
    }

    fun getFreeSlotsUsed(): Int {
        return settings.getInt(FREE_SLOTS_USED_KEY, 0)
    }

    fun saveAppLaunchCount(count: Int) {
        settings.putInt(APP_LAUNCH_COUNT_KEY, count)
    }

    fun getAppLaunchCount(): Int {
        return settings.getInt(APP_LAUNCH_COUNT_KEY, 0)
    }

    fun saveReviewStatus(status: Int) {
        settings.putInt(REVIEW_STATUS_KEY, status)
    }

    fun getReviewStatus(): Int {
        return settings.getInt(REVIEW_STATUS_KEY, 0)
    }

    fun saveLastGiftTimestamp(timestamp: Long) {
        settings.putLong(LAST_GIFT_TIMESTAMP_KEY, timestamp)
    }

    fun getLastGiftTimestamp(): Long {
        return settings.getLong(LAST_GIFT_TIMESTAMP_KEY, 0L)
    }

    fun saveCounterReadingSetting(isEnabled: Boolean) {
        settings.putBoolean(COUNTER_READING_SETTING_KEY, isEnabled)
    }

    fun getCounterReadingSetting(): Boolean {
        return settings.getBoolean(COUNTER_READING_SETTING_KEY, false)
    }

    fun saveTtsSpeechRate(rate: Float) {
        settings.putFloat(TTS_SPEECH_RATE_KEY, rate)
    }

    fun getTtsSpeechRate(): Float {
        return settings.getFloat(TTS_SPEECH_RATE_KEY, 1.5f)
    }

    fun saveTtsEngine(engine: String) {
        settings.putString(TTS_ENGINE_KEY, engine)
    }

    fun getTtsEngine(): String {
        return settings.getString(TTS_ENGINE_KEY, "")
    }

    fun saveTtsLanguage(language: String) {
        settings.putString(TTS_LANGUAGE_KEY, language)
    }

    fun getTtsLanguage(): String {
        return settings.getString(TTS_LANGUAGE_KEY, "")
    }

    fun saveAdsRemoved(areAdsRemoved: Boolean) {
        settings.putBoolean(ARE_ADS_REMOVED_KEY, areAdsRemoved)
    }

    fun areAdsRemoved(): Boolean {
        return settings.getBoolean(ARE_ADS_REMOVED_KEY, false)
    }

    companion object {
        private const val COUNTERS_KEY = "counters_list"
        private const val LAST_SELECTED_ID_KEY = "last_selected_counter_id"
        private const val SOUND_SETTING_KEY = "sound_setting"
        private const val VIBRATION_SETTING_KEY = "vibration_setting"
        private const val NIGHT_MODE_SETTING_KEY = "night_mode_setting"
        private const val BACKGROUND_SETTING_KEY = "background_setting"
        private const val DISPLAY_THEME_SETTING_KEY = "display_theme_setting"
        private const val FULL_SCREEN_TOUCH_KEY = "full_screen_touch_setting"
        private const val BACKUP_SETTING_KEY = "backup_setting_key"
        private const val UNLOCKED_ITEMS_KEY = "unlocked_items"
        private const val FREE_SLOTS_USED_KEY = "free_slots_used_count"
        private const val APP_LAUNCH_COUNT_KEY = "app_launch_count"
        private const val REVIEW_STATUS_KEY = "review_status"
        private const val LAST_GIFT_TIMESTAMP_KEY = "last_gift_timestamp"
        private const val COUNTER_READING_SETTING_KEY = "counter_reading_setting"
        private const val TTS_SPEECH_RATE_KEY = "tts_speech_rate"
        private const val TTS_ENGINE_KEY = "tts_engine_key"
        private const val TTS_LANGUAGE_KEY = "tts_language_key"
        private const val LAST_UPDATE_CHECK_TIMESTAMP_KEY = "last_update_check_timestamp"
        private const val ARE_ADS_REMOVED_KEY = "are_ads_removed"
    }
}