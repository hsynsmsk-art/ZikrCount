package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.data.Counter
import com.hgtcsmsk.zikrcount.platform.BannerAd
import com.hgtcsmsk.zikrcount.platform.PlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.SoundPlayer
import com.hgtcsmsk.zikrcount.ui.components.CounterDisplay
import com.hgtcsmsk.zikrcount.ui.components.SmallActionButton
import com.hgtcsmsk.zikrcount.ui.components.SuccessSnackBar
import com.hgtcsmsk.zikrcount.ui.components.TopActionButton
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun HomePage(
    viewModel: AppViewModel,
    onNavigateToCounters: () -> Unit,
    onNavigateToTheme: () -> Unit,
    onNavigateToSettings: () -> Unit,
    platformActionHandler: PlatformActionHandler,
    soundPlayer: SoundPlayer,
    windowSizeClass: WindowSizeClass
) {
    val counters by viewModel.counters.collectAsState()
    val selectedId by viewModel.lastSelectedCounterId.collectAsState()
    val selectedCounter: Counter? = counters.find { it.id == selectedId }

    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
    val isFullScreenTouchEnabled by viewModel.isFullScreenTouchEnabled.collectAsState()
    val selectedBackground by viewModel.selectedBackground.collectAsState()
    val showBackupDialog by viewModel.showBackupConfirmationDialog.collectAsState()
    val flashAlpha = remember { Animatable(0f) }
    val shouldShowRateDialog by viewModel.shouldShowRateDialog.collectAsState()

    val showUpdateBadge by viewModel.showUpdateBadge.collectAsState()
    val updateBadgeText = stringResource(Res.string.accessibility_update_available)

    val zikirName = when (selectedCounter?.id) {
        null -> ""
        AppViewModel.DEFAULT_COUNTER.id -> stringResource(Res.string.default_counter_name)
        AppViewModel.NAMAZ_HABILITATES_COUNTER.id -> {
            selectedCounter.let { counter ->
                when (counter.count) {
                    in 0..32 -> stringResource(Res.string.prayer_tasbih_part1)
                    in 33..65 -> stringResource(Res.string.prayer_tasbih_part2)
                    in 66..98 -> stringResource(Res.string.prayer_tasbih_part3)
                    else -> stringResource(Res.string.prayer_tasbih_counter_name)
                }
            }
        }
        else -> selectedCounter.name
    }

    var showResetDialog by remember { mutableStateOf(false) }
    var isMenuExpanded by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()
    val snackBarHostState = remember { SnackbarHostState() }
    val resetMessage = stringResource(Res.string.snackbar_counter_reset)
    val resetAnnouncementTemplate = stringResource(Res.string.snackbar_counter_reset)


    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collect { event ->
            when (event) {
                is AppViewModel.UiEvent.ShowSnackbar -> {
                    snackBarHostState.showSnackbar(event.message)
                }
            }
        }
    }

    val turTextScale = remember { Animatable(1f) }
    val turCompletedEvent by viewModel.turCompletedEvent.collectAsState()
    val flashEffectEvent by viewModel.flashEffectEvent.collectAsState()

    val bannerAdTrigger = 0
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()

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


    val performIncrementAction = { isFullScreen: Boolean ->
        scope.launch {
            if (isMenuExpanded) isMenuExpanded = false
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
            if (vibrationEnabled) platformActionHandler.performCustomVibration()
            if (soundEnabled) soundPlayer.play("mini_click", volume = 0.6f)
        }
    }

    val fullScreenTouchModifier = if (isFullScreenTouchEnabled) {
        Modifier.pointerInput(Unit) {
            awaitPointerEventScope {
                while (true) {
                    awaitFirstDown(requireUnconsumed = false)
                    performIncrementAction(true)
                    waitForUpOrCancellation()
                }
            }
        }
    } else Modifier


    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(findBackgroundResource(selectedBackground)),
            modifier = Modifier.fillMaxSize(),
            contentDescription = stringResource(Res.string.content_desc_app_background),
            contentScale = ContentScale.Crop
        )

        if (isNightModeEnabled) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.8f))
            )
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(ZikrTheme.colors.primary.copy(alpha = flashAlpha.value))
                .then(fullScreenTouchModifier)
        )

        Column(
            modifier = Modifier
                .fillMaxSize()
                .systemBarsPadding(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth(0.98f)
                        .weight(10f),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TopActionButton(
                        iconResource = Res.drawable.brush,
                        contentDescription = stringResource(Res.string.content_desc_theme_button),
                        onClick = {
                            if (soundEnabled) soundPlayer.play("mini_click")
                            isMenuExpanded = false
                            onNavigateToTheme()
                        }
                    )

                    Box(contentAlignment = Alignment.TopEnd) {
                        val settingsContentDesc = if (showUpdateBadge) {
                            stringResource(Res.string.content_desc_settings_button) + ", " + updateBadgeText
                        } else {
                            stringResource(Res.string.content_desc_settings_button)
                        }

                        TopActionButton(
                            iconResource = Res.drawable.setting,
                            contentDescription = settingsContentDesc,
                            onClick = {
                                if (soundEnabled) soundPlayer.play("mini_click")
                                isMenuExpanded = false
                                onNavigateToSettings()
                            }
                        )
                        if (showUpdateBadge) {
                            Box(
                                modifier = Modifier
                                    .padding(4.dp)
                                    .size(10.dp)
                                    .background(Color.Red, CircleShape)
                                    .border(1.dp, Color.White, CircleShape)
                            )
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(70f),
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
                            turModifier = turModifier,
                            isLandscape = false
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
                    val listButtonResource = if (isNightModeEnabled) Res.drawable.small_list_dark else Res.drawable.small_list_light
                    SmallActionButton(
                        iconResource = listButtonResource,
                        contentDescription = stringResource(Res.string.content_desc_counters_list_button),
                        onClick = {
                            if (soundEnabled) soundPlayer.play("mini_click")
                            isMenuExpanded = false
                            onNavigateToCounters()
                        }
                    )

                    ExpandingMenu(
                        isExpanded = isMenuExpanded,
                        isNightModeEnabled = isNightModeEnabled,
                        onToggle = { isMenuExpanded = !isMenuExpanded },
                        soundEnabled = soundEnabled,
                        soundPlayer = soundPlayer,
                        onShowResetDialog = { showResetDialog = true },
                        onDecrement = { performDecrementAction() }
                    )
                }

                val scale = remember { Animatable(1.0f) }
                val incrementDescription = stringResource(Res.string.content_desc_increment_button)
                Box(
                    modifier = Modifier
                        .weight(30f)
                        .aspectRatio(1f)
                        .clip(CircleShape)
                        .background(color = ZikrTheme.colors.primary)
                        .semantics {
                            contentDescription = incrementDescription
                            role = Role.Button
                        }
                        .pointerInput(Unit) {
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
                        },
                    contentAlignment = Alignment.Center
                ) {
                    val buttonResource = if (isNightModeEnabled) {
                        Res.drawable.big_button_dark
                    } else {
                        Res.drawable.big_button_light
                    }
                    Image(
                        painter = painterResource(buttonResource),
                        contentDescription = null,
                        modifier = Modifier
                            .fillMaxSize(0.97f)
                            .graphicsLayer {
                                scaleX = scale.value
                                scaleY = scale.value
                            }
                    )
                }
                Spacer(modifier = Modifier.fillMaxWidth().weight(15f))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
                    .navigationBarsPadding(),
                contentAlignment = Alignment.Center
            ) {
                if (isNetworkAvailable && purchaseState !is PurchaseState.Purchased) {
                    BannerAd(
                        modifier = Modifier.fillMaxWidth(),
                        trigger = bannerAdTrigger
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .padding(horizontal = 24.dp)
            ) { data ->
                SuccessSnackBar(data = data)
            }
        }
    }

    if (shouldShowRateDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_rate_title),
            question = stringResource(Res.string.dialog_rate_message),
            confirmButtonText = stringResource(Res.string.settings_rate),
            onDismiss = { viewModel.onRateDialogDismissed(isLater = true) },
            onConfirm = {
                platformActionHandler.launchInAppReview()
                viewModel.onRateDialogDismissed(isLater = false)
            }
        )
    }

    if (showBackupDialog) {
        val accountType = stringResource(Res.string.common_google)
        val dialogMessage = stringResource(Res.string.dialog_backup_message, accountType)
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_backup_title),
            question = dialogMessage,
            confirmButtonText = stringResource(Res.string.action_confirm),
            onDismiss = { viewModel.onBackupConfirmationResult(false) },
            onConfirm = { viewModel.onBackupConfirmationResult(true) }
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
                if (soundEnabled) soundPlayer.play("mini_click")
                viewModel.resetSelectedCounter(resetAnnouncementTemplate)
                showResetDialog = false
                scope.launch {
                    snackBarHostState.showSnackbar(resetMessage)
                }
            }
        )
    }
}


@Composable
fun ExpandingMenu(
    isExpanded: Boolean,
    isNightModeEnabled: Boolean,
    onToggle: () -> Unit,
    soundEnabled: Boolean,
    soundPlayer: SoundPlayer,
    onShowResetDialog: () -> Unit,
    onDecrement: () -> Unit
) {

    val transition = updateTransition(targetState = isExpanded, label = "menu_transition")

    val subButtonAlpha by transition.animateFloat(
        label = "sub_button_alpha",
        transitionSpec = { tween(durationMillis = 200) }
    ) { isExpandedState ->
        if (isExpandedState) 1f else 0f
    }

    val animationRadius = 55.dp

    val refreshButtonOffsetX by transition.animateDp(
        label = "refresh_offset_x",
        transitionSpec = { spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium) }
    ) { isExpandedState ->
        if (isExpandedState) (animationRadius.value * cos(45 * PI / 180f)).toFloat().dp else 0.dp
    }
    val refreshButtonOffsetY by transition.animateDp(
        label = "refresh_offset_y",
        transitionSpec = { spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium) }
    ) { isExpandedState ->
        if (isExpandedState) -(animationRadius.value * sin(45 * PI / 180f)).toFloat().dp else 0.dp
    }
    val decreaseButtonOffsetX by transition.animateDp(
        label = "decrease_offset_x",
        transitionSpec = { spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium) }
    ) { isExpandedState ->
        if (isExpandedState) (animationRadius.value * cos(135 * PI / 180f)).toFloat().dp else 0.dp
    }
    val decreaseButtonOffsetY by transition.animateDp(
        label = "decrease_offset_y",
        transitionSpec = { spring(dampingRatio = 0.6f, stiffness = Spring.StiffnessMedium) }
    ) { isExpandedState ->
        if (isExpandedState) -(animationRadius.value * sin(135 * PI / 180f)).toFloat().dp else 0.dp
    }

    val expandedStateText = stringResource(Res.string.content_desc_counter_actions_expanded)
    val collapsedStateText = stringResource(Res.string.content_desc_counter_actions_collapsed)

    Box(contentAlignment = Alignment.Center) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(x = refreshButtonOffsetX, y = refreshButtonOffsetY)
                .graphicsLayer { alpha = subButtonAlpha }
                .clickable(enabled = isExpanded) {}
        ) {
            SmallActionButton(
                modifier = Modifier.matchParentSize(),
                iconResource = Res.drawable.small_refresh,
                contentDescription = stringResource(Res.string.content_desc_reset_button),
                applyManualStroke = true,
                onClick = {
                    if (isExpanded) {
                        if (soundEnabled) soundPlayer.play("mini_click")
                        onShowResetDialog()
                        onToggle()
                    }
                }
            )
        }

        Box(
            modifier = Modifier
                .size(40.dp)
                .offset(x = decreaseButtonOffsetX, y = decreaseButtonOffsetY)
                .graphicsLayer { alpha = subButtonAlpha }
                .clickable(enabled = isExpanded) {}
        ) {
            SmallActionButton(
                modifier = Modifier.matchParentSize(),
                iconResource = Res.drawable.small_decrease,
                contentDescription = stringResource(Res.string.content_desc_decrement_button),
                applyManualStroke = true,
                onClick = {
                    if(isExpanded) { onDecrement() }
                }
            )
        }


        val setButtonResource = if (isNightModeEnabled) Res.drawable.small_set_dark else Res.drawable.small_set_light
        SmallActionButton(
            iconResource = setButtonResource,
            contentDescription = stringResource(Res.string.content_desc_counter_actions),
            onClick = {
                if (soundEnabled) soundPlayer.play("mini_click")
                onToggle()
            },
            modifier = Modifier.semantics {
                stateDescription = if (isExpanded) expandedStateText else collapsedStateText
            }
        )
    }
}