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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
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
import com.hgtcsmsk.zikrcount.ui.components.TabletActionButton
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.utils.pressable
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

@Composable
fun TabletCountersPageContent(
    viewModel: AppViewModel,
    soundPlayer: SoundPlayer,
    snackBarHostState: SnackbarHostState
) {
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
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(color = ZikrTheme.colors.primary)
                            .pressable {
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
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        val addButtonResource = if (isNightModeEnabled) Res.drawable.add_dark else Res.drawable.add_light
                        Image(
                            painter = painterResource(addButtonResource),
                            contentDescription = stringResource(Res.string.counters_page_add_button),
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
    onToggle: () -> Unit
) {
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
            if (soundEnabled) {
                soundPlayer.play("target", volume = 0.4f)
            }
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

    // --> DEĞİŞİKLİK: Hatalı fonksiyon çağrısını düzeltiyoruz.
    val performIncrementAction = { isFullScreen: Boolean ->
        scope.launch {
            viewModel.incrementSelectedCounter(isFullScreenTap = isFullScreen)
            if (vibrationEnabled) {
                platformActionHandler.performCustomVibration()
            }
            if (soundEnabled) {
                soundPlayer.play("audio_click", volume = 0.6f)
            }
        }
    }

    val performDecrementAction = {
        scope.launch {
            viewModel.decrementSelectedCounter()
            if (vibrationEnabled) {
                platformActionHandler.performCustomVibration()
            }
            if (soundEnabled) {
                soundPlayer.play("mini_click", volume = 0.6f)
            }
        }
    }

    val fullScreenTouchModifier = if (isFullScreenTouchEnabled) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = true)
                    performIncrementAction(true)
                    waitForUpOrCancellation()
                }
            }
        }
    } else Modifier

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(24.dp))
            .background(Color.Black.copy(alpha = 0.4f))
            .border(
                width = 2.dp,
                color = ZikrTheme.colors.primary.copy(alpha = 0.7f),
                shape = RoundedCornerShape(24.dp)
            )
            .then(fullScreenTouchModifier)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZikrTheme.colors.primary.copy(alpha = flashAlpha.value))
        )

        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.weight(9f))

            Box(
                modifier = Modifier.fillMaxSize().weight(70f),
                contentAlignment = Alignment.Center
            ) {
                val turModifier = Modifier.graphicsLayer {
                    scaleX = turTextScale.value
                    scaleY = turTextScale.value
                }
                if (selectedCounter != null) {
                    CounterDisplay(
                        counter = selectedCounter,
                        countName = zikirName,
                        modifier = Modifier.fillMaxWidth(0.82f),
                        screenResource = Res.drawable.screen,
                        turModifier = turModifier
                    )
                }
            }

            Spacer(modifier = Modifier.fillMaxWidth().weight(7f))

            Row(
                modifier = Modifier
                    .fillMaxWidth(0.65f)
                    .weight(9f),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                val decreaseButtonResource = if (isNightModeEnabled) Res.drawable.small_decrease_tab_dark else Res.drawable.small_decrease_tab_light
                TabletActionButton(
                    modifier = Modifier.size(48.dp),
                    iconResource = decreaseButtonResource, // <-- GÜNCELLENDİ
                    contentDescription = stringResource(Res.string.content_desc_decrement_button),
                    tintIcon = false,
                    onClick = {
                        performDecrementAction()
                    }
                )

                TabletActionButton(
                    modifier = Modifier.size(48.dp),
                    iconResource = Res.drawable.small_refresh_tab,
                    contentDescription = stringResource(Res.string.content_desc_reset_button),
                    tintIcon = false,
                    onClick = {
                        if (soundEnabled) {
                            soundPlayer.play("mini_click", volume = 0.6f)
                        }
                        showResetDialog = true
                    }
                )
            }

            val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
            val scale = remember { Animatable(1.0f) }

            Box(
                modifier = Modifier
                    .weight(26f)
                    .aspectRatio(1f)
                    .clip(CircleShape)
                    .background(color = ZikrTheme.colors.primary),
                contentAlignment = Alignment.Center
            ) {
                // Gece modu durumuna göre doğru görsel kaynağını seçiyoruz
                val buttonResource = if (isNightModeEnabled) {
                    Res.drawable.big_button_dark
                } else {
                    Res.drawable.big_button_light
                }

                Image(
                    painter = painterResource(buttonResource), // <-- DÜZELTİLDİ
                    contentDescription = stringResource(Res.string.content_desc_increment_button),
                    modifier = Modifier
                        .fillMaxSize(0.97f)
                        .graphicsLayer {
                            scaleX = scale.value
                            scaleY = scale.value
                        }
                        .pointerInput(isFullScreenTouchEnabled) {
                            if (!isFullScreenTouchEnabled) {
                                awaitPointerEventScope {
                                    while (true) {
                                        awaitFirstDown(requireUnconsumed = false)
                                        scope.launch {
                                            scale.animateTo(0.93f, tween(15))
                                            scale.animateTo(1.0f, tween(30))
                                        }
                                        performIncrementAction(false)
                                        waitForUpOrCancellation()
                                    }
                                }
                            }
                        }
                )
            }

            Spacer(modifier = Modifier.fillMaxWidth().weight(20f))
        }

        TabletActionButton(
            iconResource = Res.drawable.small_back_tab,
            contentDescription = stringResource(Res.string.content_desc_toggle_panels_button),
            onClick = { onToggle() },
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(top = 20.dp, start = 20.dp)
                .size(44.dp)
                .rotate(rotationAngle)
        )
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
                viewModel.resetSelectedCounter()
                showResetDialog = false
                scope.launch {
                    snackBarHostState.showSnackbar(resetMessage)
                }
            }
        )
    }
}