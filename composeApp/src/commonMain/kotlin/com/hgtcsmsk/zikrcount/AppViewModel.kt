package com.hgtcsmsk.zikrcount

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.hgtcsmsk.zikrcount.data.Counter
import com.hgtcsmsk.zikrcount.data.CounterStorage
import com.hgtcsmsk.zikrcount.data.UpdateService
import com.hgtcsmsk.zikrcount.data.UpdateState
import com.hgtcsmsk.zikrcount.data.createSettings
import com.hgtcsmsk.zikrcount.platform.BillingService
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.RestoreResult
import com.hgtcsmsk.zikrcount.platform.TtsEngineInfo
import com.hgtcsmsk.zikrcount.platform.TtsManager
import com.hgtcsmsk.zikrcount.platform.createBillingService
import com.hgtcsmsk.zikrcount.platform.getAppLanguageCode
import com.hgtcsmsk.zikrcount.platform.getAppVersionCode
import com.hgtcsmsk.zikrcount.platform.getLocalizedString
import com.hgtcsmsk.zikrcount.platform.getTtsEngines
import com.hgtcsmsk.zikrcount.platform.isAccessibilityServiceEnabled
import com.hgtcsmsk.zikrcount.platform.isInternetAvailable
import com.hgtcsmsk.zikrcount.ui.utils.format
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock

class AppViewModel : ViewModel() {

    private val storage = CounterStorage(createSettings())
    private val updateService = UpdateService()
    private val billingService: BillingService = createBillingService()

    private val _isLoading = MutableStateFlow(true)
    val isLoading = _isLoading.asStateFlow()

    private val _updateState = MutableStateFlow<UpdateState>(UpdateState.NoUpdate)
    val updateState = _updateState.asStateFlow()

    private val _showUpdateBadge = MutableStateFlow(false)
    val showUpdateBadge = _showUpdateBadge.asStateFlow()

    private val _counters = MutableStateFlow<List<Counter>>(emptyList())
    val counters = _counters.asStateFlow()

    private val _lastSelectedCounterId = MutableStateFlow(0L)
    val lastSelectedCounterId = _lastSelectedCounterId.asStateFlow()

    private val _soundEnabled = MutableStateFlow(true)
    val soundEnabled = _soundEnabled.asStateFlow()

    private val _vibrationEnabled = MutableStateFlow(true)
    val vibrationEnabled = _vibrationEnabled.asStateFlow()

    private val _isNightModeEnabled = MutableStateFlow(false)
    val isNightModeEnabled = _isNightModeEnabled.asStateFlow()

    private val _selectedBackground = MutableStateFlow("background_1")
    val selectedBackground = _selectedBackground.asStateFlow()

    private val _selectedDisplayTheme = MutableStateFlow("blueTurquoise")
    val selectedDisplayTheme = _selectedDisplayTheme.asStateFlow()

    private val _isFullScreenTouchEnabled = MutableStateFlow(false)
    val isFullScreenTouchEnabled = _isFullScreenTouchEnabled.asStateFlow()

    private val _isBackupEnabled = MutableStateFlow(false)
    val isBackupEnabled = _isBackupEnabled.asStateFlow()

    private val _showBackupConfirmationDialog = MutableStateFlow(false)
    val showBackupConfirmationDialog = _showBackupConfirmationDialog.asStateFlow()

    private val _unlockedItems = MutableStateFlow<Set<String>>(emptySet())
    val unlockedItems = _unlockedItems.asStateFlow()

    private val _turCompletedEvent = MutableStateFlow<Unit?>(null)
    val turCompletedEvent = _turCompletedEvent.asStateFlow()

    private val _flashEffectEvent = MutableStateFlow<Unit?>(null)
    val flashEffectEvent = _flashEffectEvent.asStateFlow()

    private val _isAdPlaying = MutableStateFlow(false)
    val isAdPlaying = _isAdPlaying.asStateFlow()

    private val _eventChannel = Channel<UiEvent>()
    val eventFlow = _eventChannel.receiveAsFlow()
    sealed class UiEvent {
        data class ShowSnackbar(val message: String) : UiEvent()
    }

    private val _shouldShowRateDialog = MutableStateFlow(false)
    val shouldShowRateDialog = _shouldShowRateDialog.asStateFlow()

    private val _isNetworkAvailable = MutableStateFlow(true)
    val isNetworkAvailable = _isNetworkAvailable.asStateFlow()

    private val _freeSlotsUsed = MutableStateFlow(0)
    val freeSlotsUsed = _freeSlotsUsed.asStateFlow()

    private val _adFailureCount = MutableStateFlow(0)
    val adFailureCount = _adFailureCount.asStateFlow()

    private val _isShowingAdLoadingIndicator = MutableStateFlow(false)
    val isShowingAdLoadingIndicator = _isShowingAdLoadingIndicator.asStateFlow()

    private val _adRetryTrigger = MutableStateFlow(0)
    val adRetryTrigger = _adRetryTrigger.asStateFlow()

    private var lastAdLoadAttemptTimestamp: Long = 0L

    private val _isCounterReadingEnabled = MutableStateFlow(false)
    val isCounterReadingEnabled = _isCounterReadingEnabled.asStateFlow()

    private val _ttsSpeechRate = MutableStateFlow(1.5f)
    val ttsSpeechRate = _ttsSpeechRate.asStateFlow()

    private val _ttsEngines = MutableStateFlow<List<TtsEngineInfo>>(emptyList())
    val ttsEngines = _ttsEngines.asStateFlow()

    private val _availableTtsLanguages = MutableStateFlow<List<Pair<String, String>>>(emptyList())
    val availableTtsLanguages = _availableTtsLanguages.asStateFlow()

    private val _selectedTtsEngine = MutableStateFlow("")
    val selectedTtsEngine = _selectedTtsEngine.asStateFlow()

    private val _selectedTtsLanguage = MutableStateFlow("")
    val selectedTtsLanguage = _selectedTtsLanguage.asStateFlow()

    private val _isLanguageLoading = MutableStateFlow(false)
    val isLanguageLoading = _isLanguageLoading.asStateFlow()

    val purchaseState = billingService.purchaseState
    val productPrice = billingService.productPrice

    private val _isRestoringPurchases = MutableStateFlow(false)
    val isRestoringPurchases = _isRestoringPurchases.asStateFlow()

    private val _shouldShowTalkbackPrompt = MutableStateFlow(false)
    val shouldShowTalkbackPrompt = _shouldShowTalkbackPrompt.asStateFlow()


    init {
        _ttsEngines
            .onEach { engines ->
                if (engines.isNotEmpty() && storage.getTtsEngine().isEmpty()) {
                    val googleEngine = engines.find { it.name.contains("google", ignoreCase = true) }
                    val defaultEngine = googleEngine ?: engines.first()
                    setSelectedTtsEngine(defaultEngine.name)
                }
            }
            .launchIn(viewModelScope)

        TtsManager.availableLanguages
            .onEach { languages ->
                _availableTtsLanguages.value = languages
                _isLanguageLoading.value = false

                if (storage.getTtsLanguage().isEmpty() && languages.isNotEmpty()) {
                    val appLangCode = getAppLanguageCode()
                    val phoneLanguage = languages.find { it.first.startsWith(appLangCode, ignoreCase = true) }
                    val englishLanguage = languages.find { it.first.startsWith("en", ignoreCase = true) }

                    setSelectedTtsLanguage((phoneLanguage ?: englishLanguage ?: languages.first()).first)
                }
            }
            .launchIn(viewModelScope)

        purchaseState.onEach { state ->
            storage.saveAdsRemoved(state is PurchaseState.Purchased)
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            try {
                withContext(Dispatchers.IO) {
                    billingService.restorePurchases()
                    _counters.value = reorderCounters(storage.getCounters())
                    _lastSelectedCounterId.value = storage.getLastSelectedCounterId()
                    _soundEnabled.value = storage.getSoundSetting()
                    _vibrationEnabled.value = storage.getVibrationSetting()
                    _isNightModeEnabled.value = storage.getNightModeSetting()
                    _selectedBackground.value = storage.getBackgroundSetting()
                    _selectedDisplayTheme.value = storage.getDisplayThemeSetting()
                    _isFullScreenTouchEnabled.value = storage.getIsFullScreenTouchEnabled()
                    _isBackupEnabled.value = storage.getBackupSetting()
                    _unlockedItems.value = storage.getUnlockedItems()
                    _freeSlotsUsed.value = storage.getFreeSlotsUsed()
                    _isCounterReadingEnabled.value = storage.getCounterReadingSetting()
                    _ttsSpeechRate.value = storage.getTtsSpeechRate()
                    _selectedTtsEngine.value = storage.getTtsEngine()
                    _selectedTtsLanguage.value = storage.getTtsLanguage()

                    loadTtsEngines()

                    if (storage.getTtsEngine().isNotEmpty()) {
                        TtsManager.reconfigure(_selectedTtsEngine.value.ifEmpty { null }, _selectedTtsLanguage.value)
                    }

                    if (_counters.value.isEmpty()) {
                        setupDefaultCounters()
                    }

                    if (_counters.value.find { it.id == _lastSelectedCounterId.value } == null) {
                        _lastSelectedCounterId.value = DEFAULT_COUNTER.id
                        storage.saveLastSelectedCounterId(DEFAULT_COUNTER.id)
                    }

                    _isNetworkAvailable.value = isInternetAvailable()

                    val launchCount = storage.getAppLaunchCount() + 1
                    storage.saveAppLaunchCount(launchCount)
                    handleReviewLogic(launchCount)
                    if (!storage.getIsTalkbackPromptShown()) {
                        checkAccessibilityStatusOnResume()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
                checkForUpdates()
            }
        }
    }

    fun purchaseRemoveAds(activity: Any) {
        billingService.purchaseRemoveAds(activity)
    }

    fun restorePurchases(noPurchaseMessage: String, errorMessage: String) {
        viewModelScope.launch {
            _isRestoringPurchases.value = true
            when (billingService.restorePurchases()) {
                RestoreResult.Success -> {}
                RestoreResult.NoPurchasesFound -> _eventChannel.send(UiEvent.ShowSnackbar(noPurchaseMessage))
                RestoreResult.Error -> _eventChannel.send(UiEvent.ShowSnackbar(errorMessage))
            }
            _isRestoringPurchases.value = false
        }
    }

    fun checkForUpdates() {
        viewModelScope.launch(Dispatchers.IO) {
            val currentTime = Clock.System.now().toEpochMilliseconds()
            val lastCheck = storage.getLastUpdateCheckTimestamp()
            val checkInterval = 0L

            if (currentTime - lastCheck < checkInterval || !_isNetworkAvailable.value) {
                return@launch
            }

            val remoteUpdateInfo = updateService.getUpdateInfo()
            if (remoteUpdateInfo == null) {
                println("DEBUG: Failed to retrieve update JSON!")
                return@launch
            }

            val currentVersionCode = getAppVersionCode()
            val newState = when {
                currentVersionCode < remoteUpdateInfo.minimumRequiredVersionCode -> UpdateState.Mandatory(remoteUpdateInfo)
                currentVersionCode < remoteUpdateInfo.latestVersionCode -> UpdateState.Optional(remoteUpdateInfo)
                else -> UpdateState.NoUpdate
            }

            _showUpdateBadge.value = newState is UpdateState.Optional
            _updateState.value = newState
            storage.saveLastUpdateCheckTimestamp(currentTime)
        }
    }

    fun dismissUpdateDialog() {
        _updateState.value = UpdateState.NoUpdate
        _showUpdateBadge.value = false
    }

    fun onUpdateBannerClicked() {
        _showUpdateBadge.value = false
    }

    private fun setupDefaultCounters() {
        _counters.value = listOf(DEFAULT_COUNTER, NAMAZ_HABILITATES_COUNTER)
        storage.saveCounters(_counters.value)
        _lastSelectedCounterId.value = DEFAULT_COUNTER.id
        storage.saveLastSelectedCounterId(DEFAULT_COUNTER.id)
    }

    private fun handleReviewLogic(launchCount: Int) {
        val reviewStatus = storage.getReviewStatus()
        if (reviewStatus == -1) return
        if ((reviewStatus == 0 && launchCount >= 5) || (reviewStatus == 1 && launchCount >= 50)) {
            _shouldShowRateDialog.value = true
        }
    }

    fun onNetworkAvailable() {
        if (!_isNetworkAvailable.value) {
            _isNetworkAvailable.value = true
            checkForUpdates()
        }
    }

    fun onNetworkLost() {
        _isNetworkAvailable.value = false
    }

    fun onRateDialogDismissed(isLater: Boolean) {
        val currentStatus = storage.getReviewStatus()
        if (isLater) {
            if (currentStatus == 0) storage.saveReviewStatus(1)
        } else {
            storage.saveReviewStatus(-1)
        }
        _shouldShowRateDialog.value = false
    }

    private fun reorderCounters(counters: List<Counter>): List<Counter> {
        val defaultCounter = counters.find { it.id == DEFAULT_COUNTER.id }
        val tasbihCounter = counters.find { it.id == NAMAZ_HABILITATES_COUNTER.id }
        val pinnedCounters = counters.filter { it.pinTimestamp > 0L }.sortedByDescending { it.pinTimestamp }
        val remainingCounters = counters.filter {
            it.pinTimestamp == 0L && it.id != DEFAULT_COUNTER.id && it.id != NAMAZ_HABILITATES_COUNTER.id
        }.sortedBy { it.creationTimestamp }
        return listOfNotNull(defaultCounter, tasbihCounter) + pinnedCounters + remainingCounters
    }

    private fun speakAnnouncement(template: String, vararg args: Any?) {
        viewModelScope.launch {
            if (_isCounterReadingEnabled.value) {
                val textToSpeak = if (args.isNotEmpty()) template.format(*args) else template
                TtsManager.speak(textToSpeak, _ttsSpeechRate.value)
            }
        }
    }

    fun onTurAnimationConsumed() {
        _turCompletedEvent.value = null
    }

    fun onFlashAnimationConsumed() {
        _flashEffectEvent.value = null
    }

    fun incrementSelectedCounter(isFullScreenTap: Boolean = false) {
        if (isFullScreenTap) _flashEffectEvent.value = Unit
        val selectedId = _lastSelectedCounterId.value
        var shouldAnnounce = false
        var announcementTemplate = ""
        var announcementArgs: Array<Any?> = emptyArray()

        _counters.update { currentCounters ->
            currentCounters.map { counter ->
                if (counter.id == selectedId) {
                    val newCount = counter.count + 1
                    var newCounter = counter

                    if (counter.id == NAMAZ_HABILITATES_COUNTER.id) {
                        if (newCount >= counter.target) {
                            _turCompletedEvent.value = Unit
                            val newTur = counter.tur + 1
                            newCounter = counter.copy(count = 0, tur = newTur)
                            shouldAnnounce = true
                            val langTag = _selectedTtsLanguage.value.ifEmpty { getAppLanguageCode() }
                            announcementTemplate = getLocalizedString("accessibility_tour_completed", langTag)
                            announcementArgs = arrayOf(33, newTur)
                        } else {
                            newCounter = counter.copy(count = newCount)
                            val displayCount = newCount % 33
                            shouldAnnounce = true
                            announcementTemplate = if (displayCount == 0) "33" else "%d"
                            announcementArgs = if (displayCount == 0) emptyArray() else arrayOf(displayCount)
                        }
                    } else {
                        if (counter.target > 0 && newCount >= counter.target) {
                            _turCompletedEvent.value = Unit
                            val newTur = counter.tur + 1
                            newCounter = counter.copy(count = 0, tur = newTur)
                            shouldAnnounce = true
                            val langTag = _selectedTtsLanguage.value.ifEmpty { getAppLanguageCode() }
                            announcementTemplate = getLocalizedString("accessibility_tour_completed", langTag)
                            announcementArgs = arrayOf(counter.target, newTur)
                        } else {
                            newCounter = counter.copy(count = newCount)
                            shouldAnnounce = true
                            announcementTemplate = "%d"
                            announcementArgs = arrayOf(newCount)
                        }
                    }
                    newCounter
                } else {
                    counter
                }
            }
        }
        storage.saveCounters(_counters.value)

        if (shouldAnnounce) {
            speakAnnouncement(announcementTemplate, *announcementArgs)
        }
    }

    fun decrementSelectedCounter() {
        viewModelScope.launch {
            val selectedId = _lastSelectedCounterId.value
            var shouldAnnounce = false
            var announcementTemplate = ""
            var announcementArgs: Array<Any?> = emptyArray()
            var originalCounterBeforeUpdate: Counter? = null

            _counters.update { currentCounters ->
                originalCounterBeforeUpdate = currentCounters.find { it.id == selectedId }

                currentCounters.map { counter ->
                    if (counter.id == selectedId) {
                        val effectiveTarget = if (counter.target > 0) counter.target else 1
                        val currentTotal = (counter.tur * effectiveTarget) + counter.count
                        if (currentTotal <= 0) return@map counter

                        val newTotal = currentTotal - 1
                        val newTur: Int
                        val newCount: Int

                        if (counter.target > 0) {
                            newTur = newTotal / counter.target
                            newCount = newTotal % counter.target
                        } else {
                            newTur = 0
                            newCount = newTotal
                        }
                        counter.copy(count = newCount, tur = newTur)

                    } else {
                        counter
                    }
                }
            }
            storage.saveCounters(_counters.value)

            val originalCounter = originalCounterBeforeUpdate ?: return@launch
            val updatedCounter = _counters.value.find { it.id == selectedId } ?: return@launch

            val isTasbihCounter = originalCounter.id == NAMAZ_HABILITATES_COUNTER.id

            val isTargetedOneToZero = !isTasbihCounter && originalCounter.target > 0 &&
                    originalCounter.count == 1 && updatedCounter.count == 0

            val isTasbihOneToZeroNoRoundChange = isTasbihCounter && originalCounter.count == 1 && updatedCounter.count == 0 && updatedCounter.tur == originalCounter.tur

            val didRoundDecrease = updatedCounter.tur < originalCounter.tur

            val didCrossTasbihInnerBoundaryDown = isTasbihCounter &&
                    originalCounter.count > 0 &&
                    originalCounter.count != originalCounter.target &&
                    originalCounter.count % 33 == 0 &&
                    updatedCounter.count % 33 != 0 &&
                    !didRoundDecrease

            val didReachTasbihInnerBoundary = isTasbihCounter &&
                    originalCounter.count > 0 &&
                    originalCounter.count != originalCounter.target &&
                    originalCounter.count % 33 != 0 &&
                    updatedCounter.count % 33 == 0 &&
                    !didRoundDecrease &&
                    !isTasbihOneToZeroNoRoundChange

            shouldAnnounce = true

            when {
                isTargetedOneToZero -> {
                    val langTag = _selectedTtsLanguage.value.ifEmpty { getAppLanguageCode() }
                    announcementTemplate = getLocalizedString("accessibility_tour_completed", langTag)
                    announcementArgs = arrayOf(originalCounter.target, originalCounter.tur)
                }
                isTasbihOneToZeroNoRoundChange -> {
                    val langTag = _selectedTtsLanguage.value.ifEmpty { getAppLanguageCode() }
                    announcementTemplate = getLocalizedString("accessibility_tour_completed", langTag)
                    announcementArgs = arrayOf(33, originalCounter.tur)
                }
                didRoundDecrease -> {
                    announcementTemplate = "%d"
                    val countToRead = if (isTasbihCounter) updatedCounter.count % 33 else updatedCounter.count
                    announcementArgs = arrayOf(countToRead)
                }
                didCrossTasbihInnerBoundaryDown -> {
                    announcementTemplate = "%d"
                    announcementArgs = arrayOf(updatedCounter.count % 33)
                }
                didReachTasbihInnerBoundary -> {
                    announcementTemplate = "33"
                    announcementArgs = emptyArray()
                }
                else -> {
                    announcementTemplate = "%d"
                    val countToAnnounce = if (isTasbihCounter) {
                        updatedCounter.count % 33
                    } else {
                        updatedCounter.count
                    }
                    announcementArgs = arrayOf(countToAnnounce.coerceAtLeast(0))
                }
            }

            if (shouldAnnounce) {
                speakAnnouncement(announcementTemplate, *announcementArgs)
            }
        }
    }


    fun resetSelectedCounter(resetTemplate: String) {
        viewModelScope.launch {
            val selectedId = _lastSelectedCounterId.value
            _counters.update { currentCounters ->
                currentCounters.map {
                    if (it.id == selectedId) {
                        it.copy(count = 0, tur = 0)
                    } else {
                        it
                    }
                }
            }
            storage.saveCounters(_counters.value)
            speakAnnouncement(resetTemplate)
        }
    }

    fun onBackupSwitchToggled(isEnabled: Boolean) {
        if (isEnabled) {
            _showBackupConfirmationDialog.value = true
        } else {
            setBackupEnabled(false)
        }
    }

    fun onBackupConfirmationResult(isConfirmed: Boolean) {
        _showBackupConfirmationDialog.value = false
        if (isConfirmed) {
            setBackupEnabled(true)
        }
    }

    private fun setBackupEnabled(isEnabled: Boolean) {
        _isBackupEnabled.value = isEnabled
        storage.saveBackupSetting(isEnabled)
        com.hgtcsmsk.zikrcount.platform.setBackupEnabled(isEnabled)
    }

    fun setSoundEnabled(isEnabled: Boolean) {
        _soundEnabled.value = isEnabled
        storage.saveSoundSetting(isEnabled)
    }

    fun setVibrationEnabled(isEnabled: Boolean) {
        _vibrationEnabled.value = isEnabled
        storage.saveVibrationSetting(isEnabled)
    }

    fun setNightModeEnabled(isEnabled: Boolean) {
        _isNightModeEnabled.value = isEnabled
        storage.saveNightModeSetting(isEnabled)
    }

    fun setFullScreenTouchEnabled(isEnabled: Boolean) {
        _isFullScreenTouchEnabled.value = isEnabled
        storage.saveFullScreenTouchSetting(isEnabled)
    }

    fun setSelectedBackground(name: String) {
        _selectedBackground.value = name
        storage.saveBackgroundSetting(name)
    }

    fun setSelectedDisplayTheme(name: String) {
        _selectedDisplayTheme.value = name
        storage.saveDisplayThemeSetting(name)
    }

    fun selectCounter(counter: Counter) {
        _lastSelectedCounterId.value = counter.id
        storage.saveLastSelectedCounterId(counter.id)
    }

    fun pinCounter(counterId: Long) {
        viewModelScope.launch {
            _counters.update { currentCounters ->
                val updatedCounters = currentCounters.map { counter ->
                    if (counter.id == counterId) {
                        val newPinTimestamp = if (counter.pinTimestamp > 0L) 0L else Clock.System.now().toEpochMilliseconds()
                        counter.copy(pinTimestamp = newPinTimestamp)
                    } else { counter }
                }
                reorderCounters(updatedCounters)
            }
            storage.saveCounters(_counters.value)
        }
    }

    fun addCounter(name: String, targetStr: String) {
        viewModelScope.launch {
            val newCounter = Counter(name = name, target = targetStr.toIntOrNull() ?: 0)
            _counters.update { currentCounters -> reorderCounters(currentCounters + newCounter) }

            if (purchaseState.value !is PurchaseState.Purchased && _freeSlotsUsed.value < 2) {
                val newSlotCount = _freeSlotsUsed.value + 1
                _freeSlotsUsed.value = newSlotCount
                storage.saveFreeSlotsUsed(newSlotCount)
            }
            selectCounter(newCounter)
            storage.saveCounters(_counters.value)
        }
    }

    fun deleteCounter(counter: Counter) {
        viewModelScope.launch {
            _counters.update { currentCounters ->
                val updatedList = currentCounters.filter { c -> c.id != counter.id }
                if (_lastSelectedCounterId.value == counter.id) {
                    storage.saveLastSelectedCounterId(DEFAULT_COUNTER.id)
                    _lastSelectedCounterId.value = DEFAULT_COUNTER.id
                }
                reorderCounters(updatedList)
            }
            storage.saveCounters(_counters.value)
        }
    }

    fun updateCounter(id: Long, newName: String, newTargetStr: String) {
        viewModelScope.launch {
            _counters.update { currentCounters ->
                val updatedCounters = currentCounters.map { counter ->
                    if (counter.id == id) {
                        val finalName = newName.ifBlank { counter.name }
                        val newTarget = newTargetStr.toIntOrNull() ?: 0
                        if (newTarget == counter.target) {
                            return@map counter.copy(name = finalName)
                        }
                        val totalCount = (counter.tur * counter.target.coerceAtLeast(1)) + counter.count
                        val finalCount: Int
                        val finalTur: Int
                        if (newTarget > 0) {
                            finalTur = totalCount / newTarget
                            finalCount = totalCount % newTarget
                        } else {
                            finalTur = 0
                            finalCount = totalCount
                        }
                        counter.copy(name = finalName, target = newTarget, count = finalCount, tur = finalTur)
                    } else {
                        counter
                    }
                }
                reorderCounters(updatedCounters)
            }
            storage.saveCounters(_counters.value)
        }
    }

    fun modifyCounterCount(id: Long, amount: Int) {
        viewModelScope.launch {
            _counters.update { currentCounters ->
                currentCounters.map { counter ->
                    if (counter.id == id) {
                        if (counter.target > 0) {
                            val initialTotalCount = (counter.tur * counter.target) + counter.count
                            val prospectiveTotalCount = (initialTotalCount + amount).coerceAtLeast(0)
                            val finalTur = prospectiveTotalCount / counter.target
                            val finalCount = prospectiveTotalCount % counter.target
                            counter.copy(count = finalCount, tur = finalTur)
                        } else {
                            counter.copy(count = (counter.count + amount).coerceAtLeast(0))
                        }
                    } else {
                        counter
                    }
                }
            }
            storage.saveCounters(_counters.value)
        }
    }

    fun unlockItem(itemName: String) {
        val updatedSet = _unlockedItems.value.toMutableSet()
        updatedSet.add(itemName)
        _unlockedItems.value = updatedSet
        storage.saveUnlockedItems(updatedSet)
    }

    fun canAttemptAdLoad(): Boolean {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        if (currentTime - lastAdLoadAttemptTimestamp < 15000) { return false }
        lastAdLoadAttemptTimestamp = currentTime
        return true
    }

    fun canGrantGift(): Boolean {
        val lastGiftTime = storage.getLastGiftTimestamp()
        if (lastGiftTime == 0L) return true
        val currentTime = Clock.System.now().toEpochMilliseconds()
        val twentyFourHoursInMillis = 24 * 60 * 60 * 1000
        return currentTime - lastGiftTime > twentyFourHoursInMillis
    }

    fun recordGiftGranted() {
        val currentTime = Clock.System.now().toEpochMilliseconds()
        storage.saveLastGiftTimestamp(currentTime)
    }

    fun setAdPlayingState(isPlaying: Boolean) {
        _isAdPlaying.value = isPlaying
    }

    fun recordAdFailure() {
        _adFailureCount.update { it + 1 }
    }

    fun resetAdFailureCount() {
        _adFailureCount.value = 0
    }

    fun setShowingAdLoadingIndicator(isShowing: Boolean) {
        _isShowingAdLoadingIndicator.value = isShowing
    }

    fun triggerAdRetry() {
        _adRetryTrigger.update { it + 1 }
    }

    fun loadTtsEngines() {
        viewModelScope.launch {
            _ttsEngines.value = withContext(Dispatchers.IO) {
                getTtsEngines()
            }
        }
    }


    fun setCounterReadingEnabled(isEnabled: Boolean) {
        if (_ttsEngines.value.isEmpty() && isEnabled) return

        _isCounterReadingEnabled.value = isEnabled
        storage.saveCounterReadingSetting(isEnabled)

        if (isEnabled) {
            if (_selectedTtsEngine.value.isNotEmpty()) {
                TtsManager.reconfigure(_selectedTtsEngine.value, _selectedTtsLanguage.value)
            }
            else if (_ttsEngines.value.isNotEmpty()){
                val engines = _ttsEngines.value
                val googleEngine = engines.find { it.name.contains("google", ignoreCase = true) }
                val defaultEngine = googleEngine ?: engines.first()
                setSelectedTtsEngine(defaultEngine.name)
            }
        }
    }

    fun setTtsSpeechRate(rate: Float) {
        _ttsSpeechRate.value = rate.coerceIn(1.0f, 2.5f)
        storage.saveTtsSpeechRate(_ttsSpeechRate.value)
    }

    fun setSelectedTtsEngine(engineName: String) {
        if (engineName != _selectedTtsEngine.value) {
            _selectedTtsEngine.value = engineName
            storage.saveTtsEngine(engineName)
            _isLanguageLoading.value = true
            setSelectedTtsLanguage("")
        }
    }

    fun setSelectedTtsLanguage(language: String) {
        _selectedTtsLanguage.value = language
        storage.saveTtsLanguage(language)
        if (language.isNotEmpty()) {
            _isLanguageLoading.value = false
        }
        TtsManager.reconfigure(_selectedTtsEngine.value.ifEmpty { null }, language)
    }

    fun speakTestSound() {
        TtsManager.speak("33", _ttsSpeechRate.value)
    }

    fun checkAccessibilityStatusOnResume() {
        viewModelScope.launch(Dispatchers.IO) {
            if (storage.getIsTalkbackPromptShown()) return@launch

            val isTalkbackEnabled = isAccessibilityServiceEnabled()
            if (isTalkbackEnabled) {
                _shouldShowTalkbackPrompt.value = true
            } else {
                storage.saveIsTalkbackPromptShown(true)
            }
        }
    }

    fun onTalkbackPromptResult(isAccepted: Boolean) {
        _shouldShowTalkbackPrompt.value = false
        storage.saveIsTalkbackPromptShown(true)
        if (isAccepted) {
            setCounterReadingEnabled(true)
        }
    }

    companion object {
        val DEFAULT_COUNTER = Counter(id = -1L, name = "", target = 0)
        val NAMAZ_HABILITATES_COUNTER = Counter(id = -2L, name = "", target = 99)
    }
}