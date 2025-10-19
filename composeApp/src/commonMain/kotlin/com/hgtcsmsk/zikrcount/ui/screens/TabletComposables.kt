// TabletComposables.kt

package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.platform.PlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.RewardedAdState
import com.hgtcsmsk.zikrcount.platform.ShowAdResult
import com.hgtcsmsk.zikrcount.platform.SoundPlayer
import com.hgtcsmsk.zikrcount.platform.rememberAdController
import com.hgtcsmsk.zikrcount.ui.components.CounterDisplay
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.utils.pressable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import org.jetbrains.compose.resources.DrawableResource

@Composable
fun TabletCountersPageContent(
    viewModel: AppViewModel,
    soundPlayer: SoundPlayer,
    snackBarHostState: SnackbarHostState,
    isLandscape: Boolean
) {
    // ... Bu fonksiyonun içeriğinde değişiklik yok ...
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetAdFailureCount()
        }
    }

    val lazyListState = rememberLazyListState()
    val counters by viewModel.counters.collectAsState()
    val selectedId by viewModel.lastSelectedCounterId.collectAsState()
    val freeSlotsUsed by viewModel.freeSlotsUsed.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val isAdPlaying by viewModel.isAdPlaying.collectAsState()
    val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()

    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(Res.string.snackbar_counter_deleted)
    val savedMessage = stringResource(Res.string.snackbar_counter_saved)

    var showAddDialog by remember { mutableStateOf(false) }
    var showAdDialog by remember { mutableStateOf(false) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showAdLoadErrorDialog by remember { mutableStateOf(false) }
    var wasRewardGranted by remember { mutableStateOf(false) }
    var showGiftDialog by remember { mutableStateOf(false) }
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val adFailureCount by viewModel.adFailureCount.collectAsState()
    val isShowingAdLoadingIndicator by viewModel.isShowingAdLoadingIndicator.collectAsState()
    val adRetryTrigger by viewModel.adRetryTrigger.collectAsState()

    val adController = rememberAdController(
        viewModel = viewModel,
        retryTrigger = adRetryTrigger,
        onAdFailedToLoad = { errorMsg ->
            println("Silent Ad Load Failed: $errorMsg")
        }
    )

    LaunchedEffect(adController.adState) {
        if (adController.adState == RewardedAdState.NOT_LOADED) {
            adController.loadRewardAd()
        }
    }

    var previousCountersSize by remember { mutableStateOf(counters.size) }
    LaunchedEffect(counters) {
        if (counters.size > previousCountersSize) {
            val selectedIndex = counters.indexOfFirst { it.id == selectedId }
            if (selectedIndex != -1 && selectedId > 0L) {
                val visibleItemIndices = lazyListState.layoutInfo.visibleItemsInfo.map { it.index }
                if (selectedIndex !in visibleItemIndices) {
                    lazyListState.animateScrollToItem(index = selectedIndex)
                }
            }
        }
        previousCountersSize = counters.size
    }

    LaunchedEffect(isAdPlaying) {
        if (!isAdPlaying && wasRewardGranted) {
            delay(80)
            showAddDialog = true
            wasRewardGranted = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(24.dp))
                .background(Color.Black.copy(alpha = 0.4f))
                .border(
                    width = 2.dp,
                    color = ZikrTheme.colors.primary.copy(alpha = 0.7f),
                    shape = RoundedCornerShape(24.dp)
                )
        ) {
            Text(
                text = stringResource(Res.string.counters_page_title),
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )

            Box(modifier = Modifier.weight(1f)) {
                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(start = 12.dp, end = 12.dp, top = 0.dp, bottom = 80.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(counters, key = { it.id }) { counter ->
                        CounterCard(
                            counter = counter,
                            isSelected = counter.id == selectedId,
                            viewModel = viewModel,
                            onTap = { viewModel.selectCounter(counter) },
                            onDelete = {
                                viewModel.deleteCounter(counter)
                                scope.launch { snackBarHostState.showSnackbar(deletedMessage) }
                            }
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(16.dp),
                ) {
                    val fabSize = if (isLandscape) 44.dp else 50.dp
                    // <-- DEĞİŞİKLİK 2: Sayaç Ekleme Düğmesi Düzeltildi -->
                    val addCounterDescription = stringResource(Res.string.counters_page_add_button)
                    Box(
                        modifier = Modifier
                            .size(fabSize)
                            .clip(CircleShape)
                            .background(color = ZikrTheme.colors.primary)
                            .clickable {
                                if (soundEnabled) soundPlayer.play("mini_click", volume = 0.6f)
                                if (freeSlotsUsed < 2 || purchaseState is PurchaseState.Purchased) {
                                    showAddDialog = true
                                } else {
                                    if (!isNetworkAvailable) {
                                        showNoInternetDialog = true
                                    } else {
                                        showAdDialog = true
                                    }
                                }
                            }
                            // Anlamlı açıklamayı ve rolü tıklanabilir olan Box'a ekliyoruz.
                            .semantics {
                                contentDescription = addCounterDescription
                                role = Role.Button
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val addButtonResource = if (isNightModeEnabled) Res.drawable.add_dark else Res.drawable.add_light
                        Image(
                            painter = painterResource(addButtonResource),
                            // İçerideki resmi dekoratif olarak işaretliyoruz.
                            contentDescription = null,
                            modifier = Modifier.fillMaxSize(0.9f)
                        )
                    }
                }
            }
        }

        if (isShowingAdLoadingIndicator) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(24.dp))
                    .background(Color.Black.copy(alpha = 0.5f))
                    .clickable(enabled = false, onClick = {}),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = ZikrTheme.colors.primary)
            }
        }
    }

    // ... Bu fonksiyonun geri kalan diyalog kısımlarında değişiklik yok ...
    if (showAddDialog) {
        CounterUpsertDialog(
            onDismiss = { showAddDialog = false },
            onSave = { _, name, target ->
                viewModel.addCounter(name, target)
                showAddDialog = false
                scope.launch { snackBarHostState.showSnackbar(savedMessage) }
            }
        )
    }

    if (showAdDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.action_watch_ad),
            question = stringResource(Res.string.dialog_ad_add_counter_message),
            confirmButtonText = stringResource(Res.string.action_watch_ad),
            onDismiss = { showAdDialog = false },
            onConfirm = {
                showAdDialog = false
                scope.launch {
                    val adResultHandler: (ShowAdResult) -> Unit = { result ->
                        when (result) {
                            is ShowAdResult.EarnedReward -> {
                                if (result.earned) {
                                    wasRewardGranted = true
                                    viewModel.resetAdFailureCount()
                                }
                            }
                            is ShowAdResult.Failed -> {
                                if (adFailureCount >= 1) {
                                    if (viewModel.canGrantGift()) {
                                        showGiftDialog = true
                                    } else {
                                        showAdLoadErrorDialog = true
                                    }
                                } else {
                                    viewModel.recordAdFailure()
                                    showAdLoadErrorDialog = true
                                }
                            }
                            else -> {}
                        }
                    }

                    if (adController.adState == RewardedAdState.LOADED) {
                        adController.showRewardAd(adResultHandler)
                        return@launch
                    }

                    if (!viewModel.canAttemptAdLoad()) {
                        adResultHandler(ShowAdResult.Failed("Cooldown active"))
                        return@launch
                    }

                    viewModel.setShowingAdLoadingIndicator(true)
                    val adLoadedSuccessfully = withTimeoutOrNull(5000L) {
                        adController.loadRewardAd()
                    }
                    viewModel.setShowingAdLoadingIndicator(false)

                    if (adLoadedSuccessfully == true) {
                        adController.showRewardAd(adResultHandler)
                    } else {
                        adResultHandler(ShowAdResult.Failed("Ad load failed or timed out"))
                    }
                }
            }
        )
    }

    if (showAdLoadErrorDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_error_ad_load_title),
            question = stringResource(Res.string.dialog_error_ad_load_message),
            confirmButtonText = stringResource(Res.string.common_ok),
            onDismiss = {
                viewModel.triggerAdRetry()
                showAdLoadErrorDialog = false
            },
            onConfirm = {
                viewModel.triggerAdRetry()
                showAdLoadErrorDialog = false
            }
        )
    }

    if (showNoInternetDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_error_connection_title),
            question = stringResource(Res.string.dialog_error_connection_message),
            confirmButtonText = stringResource(Res.string.common_ok),
            onDismiss = { showNoInternetDialog = false },
            onConfirm = { showNoInternetDialog = false }
        )
    }

    if (showGiftDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_gift_title),
            question = stringResource(Res.string.dialog_gift_message),
            confirmButtonText = stringResource(Res.string.action_get_reward),
            onDismiss = {
                viewModel.resetAdFailureCount()
                showGiftDialog = false
            },
            onConfirm = {
                viewModel.resetAdFailureCount()
                viewModel.recordGiftGranted()
                showAddDialog = true
                showGiftDialog = false
            }
        )
    }
}

@Composable
fun TabletHomePageContent(
    viewModel: AppViewModel,
    platformActionHandler: PlatformActionHandler,
    soundPlayer: SoundPlayer,
    snackBarHostState: SnackbarHostState,
    rotationAngle: Float,
    onToggle: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToSettings: () -> Unit,
    isLandscape: Boolean,
    isTablet: Boolean
) {
    // ... Bu fonksiyonun üst kısmındaki state tanımlamaları aynı ...
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()
    val counters by viewModel.counters.collectAsState()
    val selectedId by viewModel.lastSelectedCounterId.collectAsState()
    val selectedCounter = counters.find { it.id == selectedId }
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val isFullScreenTouchEnabled by viewModel.isFullScreenTouchEnabled.collectAsState()
    val turCompletedEvent by viewModel.turCompletedEvent.collectAsState()
    val flashEffectEvent by viewModel.flashEffectEvent.collectAsState()
    val turTextScale = remember { Animatable(1f) }
    val flashAlpha = remember { Animatable(0f) }
    val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
    var showResetDialog by remember { mutableStateOf(false) }
    val resetMessage = stringResource(Res.string.snackbar_counter_reset)
    val showUpdateBadge by viewModel.showUpdateBadge.collectAsState()
    val buttonGlowModifier = Modifier.shadow(
        elevation = 8.dp,
        shape = CircleShape,
        ambientColor = ZikrTheme.colors.primary.copy(alpha = 0.5f),
        spotColor = ZikrTheme.colors.primary.copy(alpha = 0.5f)
    )
    val updateBadgeText = stringResource(Res.string.accessibility_update_available)

    val isPhoneLandscape = isLandscape && !isTablet

    LaunchedEffect(flashEffectEvent) {
        if (flashEffectEvent != null) {
            scope.launch {
                flashAlpha.animateTo(0.15f, animationSpec = tween(50))
                flashAlpha.animateTo(0f, animationSpec = tween(250))
            }
            viewModel.onFlashAnimationConsumed()
        }
    }
    LaunchedEffect(turCompletedEvent) {
        if (turCompletedEvent != null) {
            if (soundEnabled) soundPlayer.play("target", volume = 0.4f)
            scope.launch {
                turTextScale.animateTo(1.3f, animationSpec = tween(250))
                turTextScale.animateTo(1f, animationSpec = tween(250))
            }
            viewModel.onTurAnimationConsumed()
        }
    }
    val zikirName = when (selectedCounter?.id) {
        null -> ""
        AppViewModel.DEFAULT_COUNTER.id -> stringResource(Res.string.default_counter_name)
        AppViewModel.NAMAZ_TESBIHATI_COUNTER.id -> stringResource(Res.string.prayer_tasbih_counter_name)
        else -> selectedCounter.name
    }

    // HATA DÜZELTMESİ: Eksik metin parametresi eklendi
    val tourCompletedText = stringResource(Res.string.accessibility_tour_completed)

    val performIncrementAction = { isFullScreen: Boolean ->
        scope.launch {
            viewModel.incrementSelectedCounter(
                isFullScreenTap = isFullScreen,
                tourCompletedText = tourCompletedText
            )
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (soundEnabled) soundPlayer.play("audio_click", volume = 0.6f)
        }
    }
    val performDecrementAction = {
        scope.launch {
            viewModel.decrementSelectedCounter()
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            if (soundEnabled) soundPlayer.play("mini_click", volume = 0.6f)
        }
    }
    val fullScreenTouchModifier = if (isFullScreenTouchEnabled) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = true); performIncrementAction(true); waitForUpOrCancellation()
                }
            }
        }
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(width = 2.dp, color = ZikrTheme.colors.primary.copy(alpha = 0.7f), shape = RoundedCornerShape(24.dp))
            .then(fullScreenTouchModifier)
    ) {
        Box(modifier = Modifier.fillMaxSize().background(ZikrTheme.colors.primary.copy(alpha = flashAlpha.value)))

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                val topButtonSize = if (isPhoneLandscape) 36.dp else 40.dp
                TabletActionButton(
                    iconResource = Res.drawable.small_back_tab,
                    contentDescription = stringResource(Res.string.content_desc_toggle_panels_button),
                    onClick = { onToggle() },
                    modifier = Modifier.size(topButtonSize).then(buttonGlowModifier).rotate(rotationAngle)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(if(isPhoneLandscape) 8.dp else 16.dp)) {
                    TabletActionButton(
                        iconResource = Res.drawable.brush,
                        contentDescription = stringResource(Res.string.content_desc_theme_button),
                        onClick = {
                            if (soundEnabled) {
                                soundPlayer.play("mini_click")
                            }
                            onNavigateToTheme()
                        },
                        modifier = Modifier.size(topButtonSize).then(buttonGlowModifier)
                    )
                    Box(contentAlignment = Alignment.TopEnd) {
                        // <-- DEĞİŞİKLİK 4: Ayarlar Butonu Güncelleme Anonsu -->
                        val settingsDescription = if (showUpdateBadge) {
                            stringResource(Res.string.content_desc_settings_button) + updateBadgeText
                        } else {
                            stringResource(Res.string.content_desc_settings_button)
                        }
                        TabletActionButton(
                            iconResource = Res.drawable.setting,
                            contentDescription = settingsDescription,
                            onClick = {
                                if (soundEnabled) {
                                    soundPlayer.play("mini_click")
                                }
                                onNavigateToSettings()
                            },
                            modifier = Modifier.size(topButtonSize).then(buttonGlowModifier)
                        )
                        if (showUpdateBadge) {
                            Box(modifier = Modifier.padding(4.dp).size(if (isPhoneLandscape) 8.dp else 12.dp).background(Color.Red, CircleShape).border(1.dp, Color.White, CircleShape))
                        }
                    }
                }
            }

            if (!isPhoneLandscape) {
                Spacer(modifier = Modifier.weight(1f))
            }

            val displayWidthFraction = if (isPhoneLandscape) 0.85f else 0.85f
            if (selectedCounter != null) {
                val screenResource = if (isLandscape) Res.drawable.screen_horizantal else Res.drawable.screen
                CounterDisplay(
                    counter = selectedCounter,
                    countName = zikirName,
                    modifier = Modifier.fillMaxWidth(displayWidthFraction),
                    screenResource = screenResource,
                    turModifier = Modifier.graphicsLayer { scaleX = turTextScale.value; scaleY = turTextScale.value },
                    isLandscape = isLandscape,
                    isTablet = isTablet
                )
            }

            Spacer(modifier = Modifier.weight(1f))

            val buttonRowModifier = if (isLandscape) {
                Modifier
                    .fillMaxWidth(if (isPhoneLandscape) 0.95f else 0.8f)
                    .height(if (isPhoneLandscape) 65.dp else 85.dp)
            } else {
                Modifier.fillMaxWidth(0.8f)
            }

            Row(
                modifier = buttonRowModifier,
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
                verticalAlignment = Alignment.CenterVertically
            ) {
                val decreaseButtonResource = if (isNightModeEnabled) Res.drawable.small_decrease_tab_dark else Res.drawable.small_decrease_tab_light
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TabletActionButton(
                        modifier = Modifier.size(if (isPhoneLandscape) 40.dp else 50.dp).then(buttonGlowModifier),
                        iconResource = decreaseButtonResource,
                        contentDescription = stringResource(Res.string.content_desc_decrement_button),
                        tintIcon = false,
                        onClick = { performDecrementAction() }
                    )
                }

                val incrementButtonWeight = if (isPhoneLandscape) 0.8f else if (isLandscape) 1.0f else 1.6f
                val scale = remember { Animatable(1.0f) }
                // <-- DEĞİŞİKLİK 1: Ana Artırma Düğmesi Düzeltildi -->
                val incrementDescription = stringResource(Res.string.content_desc_increment_button)
                Box(
                    modifier = Modifier
                        .weight(incrementButtonWeight)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = ZikrTheme.colors.primary)
                        // Tıklama olayını ve anlamlı açıklamayı bu Box'a taşıyoruz.
                        .pointerInput(isFullScreenTouchEnabled) {
                            if (!isFullScreenTouchEnabled) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitFirstDown(requireUnconsumed = false)
                                        scope.launch { scale.animateTo(0.93f, tween(15)); scale.animateTo(1.0f, tween(30)) }
                                        performIncrementAction(false)
                                        waitForUpOrCancellation()
                                    }
                                }
                            }
                        }
                        .semantics {
                            contentDescription = incrementDescription
                            role = Role.Button
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val buttonResource = if (isNightModeEnabled) Res.drawable.big_button_dark else Res.drawable.big_button_light
                    Image(
                        painter = painterResource(buttonResource),
                        // İçerideki resmi dekoratif olarak işaretliyoruz.
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(0.97f)
                            .graphicsLayer { scaleX = scale.value; scaleY = scale.value }
                    )
                }

                val resetButtonResource = if (isNightModeEnabled) Res.drawable.small_refresh_tab_dark else Res.drawable.small_refresh_tab_light
                Box(modifier = Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    TabletActionButton(
                        modifier = Modifier.size(if (isPhoneLandscape) 40.dp else 50.dp).then(buttonGlowModifier),
                        iconResource = resetButtonResource,
                        contentDescription = stringResource(Res.string.content_desc_reset_button),
                        tintIcon = false,
                        onClick = { if (soundEnabled) soundPlayer.play("mini_click", volume = 0.6f); showResetDialog = true }
                    )
                }
            }
            Spacer(modifier = Modifier.weight(1f))
        }
    }

    if (showResetDialog) {
        val nameForDialog = when (selectedCounter?.id) {
            null -> ""
            AppViewModel.DEFAULT_COUNTER.id -> stringResource(Res.string.default_counter_name)
            else -> selectedCounter.name
        }
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_reset_counter_title),
            question = stringResource(Res.string.dialog_reset_counter_message, nameForDialog),
            confirmButtonText = stringResource(Res.string.action_reset),
            onDismiss = { showResetDialog = false },
            onConfirm = {
                if (soundEnabled) {
                    soundPlayer.play("mini_click")
                }
                viewModel.resetSelectedCounter(resetMessage)
                showResetDialog = false
            }
        )
    }
}


// <-- DEĞİŞİKLİK 3: TabletActionButton Bileşeni İyileştirildi -->
@Composable
private fun TabletActionButton(
    iconResource: DrawableResource,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tintIcon: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick() }
            // Tıklanabilir olan Box'a anlamlı açıklamayı ve rolü ekliyoruz.
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        val colorFilter = if (tintIcon) ColorFilter.tint(Color.White) else null
        Image(
            painter = painterResource(iconResource),
            // İçerideki resmi dekoratif olarak işaretliyoruz.
            contentDescription = null,
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize(0.7f)
        )
    }
}