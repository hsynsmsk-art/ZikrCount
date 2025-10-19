// composeApp/src/commonMain/kotlin/com/hgtcsmsk/zikrcount/ui/screens/CountersPage.kt

package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.data.Counter
import com.hgtcsmsk.zikrcount.platform.PurchaseState // <-- DÜZELTME: Eksik import eklendi
import com.hgtcsmsk.zikrcount.platform.RewardedAdState
import com.hgtcsmsk.zikrcount.platform.ShowAdResult
import com.hgtcsmsk.zikrcount.platform.SoundPlayer
import com.hgtcsmsk.zikrcount.platform.SystemBackButtonHandler
import com.hgtcsmsk.zikrcount.platform.formatTimestampToLocalDateTime
import com.hgtcsmsk.zikrcount.platform.rememberAdController
import com.hgtcsmsk.zikrcount.ui.components.SuccessSnackBar
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.dialog.DialogButtons
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.utils.autoMirror
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

@Composable
fun CountersPage(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    soundPlayer: SoundPlayer
) {
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetAdFailureCount()
        }
    }

    SystemBackButtonHandler { onNavigateBack() }
    val lazyListState = rememberLazyListState()
    val counters by viewModel.counters.collectAsState()
    val selectedId by viewModel.lastSelectedCounterId.collectAsState()
    val freeSlotsUsed by viewModel.freeSlotsUsed.collectAsState()
    val selectedBackground by viewModel.selectedBackground.collectAsState()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val isAdPlaying by viewModel.isAdPlaying.collectAsState()
    val purchaseState by viewModel.purchaseState.collectAsState()
    val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()

    val snackBarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val deletedMessage = stringResource(Res.string.snackbar_counter_deleted)
    val savedMessage = stringResource(Res.string.snackbar_counter_saved)

    // --- State Management ---
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

    var rememberedStatusBarHeight by remember { mutableStateOf(0.dp) }
    val currentStatusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    if (currentStatusBarHeight > 0.dp) {
        rememberedStatusBarHeight = currentStatusBarHeight
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = Color.Transparent,
        snackbarHost = {
            SnackbarHost(
                hostState = snackBarHostState,
                modifier = Modifier
                    .navigationBarsPadding()
                    .padding(bottom = 80.dp, start = 24.dp, end = 24.dp),
                snackbar = { data -> SuccessSnackBar(data = data) }
            )
        }
    ) { contentPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(findBackgroundResource(selectedBackground)),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
            )

            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rememberedStatusBarHeight)
                        .background(if (isAdPlaying) Color.Black else Color.Transparent)
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Image(
                        painter = painterResource(Res.drawable.action_back),
                        contentDescription = stringResource(Res.string.action_back),
                        colorFilter = ColorFilter.tint(Color.White),
                        modifier = Modifier
                            .padding(start = 5.dp)
                            .size(27.dp)
                            .clip(CircleShape)
                            .clickable { onNavigateBack() }
                            .autoMirror()
                    )
                    Text(
                        text = stringResource(Res.string.counters_page_title),
                        style = MaterialTheme.typography.titleLarge,
                        color = ZikrTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(32.dp))
                }

                LazyColumn(
                    state = lazyListState,
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(
                        start = 12.dp,
                        end = 12.dp,
                        top = 12.dp,
                        bottom = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding() + 80.dp
                    ),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                ) {
                    items(counters, key = { it.id }) { counter ->
                        CounterCard(
                            counter = counter,
                            isSelected = counter.id == selectedId,
                            viewModel = viewModel,
                            onTap = {
                                if (counter.id == selectedId) {
                                    onNavigateBack()
                                } else {
                                    viewModel.selectCounter(counter)
                                }
                            },
                            onDelete = {
                                viewModel.deleteCounter(counter)
                                scope.launch { snackBarHostState.showSnackbar(deletedMessage) }
                            }
                        )
                    }
                }
            }

            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(contentPadding)
                    .padding(16.dp),
            ) {
                val interactionSource = remember { MutableInteractionSource() }
                val isPressed by interactionSource.collectIsPressedAsState()
                val scale by animateFloatAsState(if (isPressed) 0.95f else 1f, label = "add-button-scale")

                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .scale(scale)
                        .clip(CircleShape)
                        .background(color = ZikrTheme.colors.primary),
                    contentAlignment = Alignment.Center
                ) {
                    val addButtonResource = if (isNightModeEnabled) Res.drawable.add_dark else Res.drawable.add_light

                    Image(
                        painter = painterResource(addButtonResource),
                        contentDescription = stringResource(Res.string.counters_page_add_button),
                        modifier = Modifier
                            .clip(CircleShape)
                            .fillMaxSize(0.9f)
                            // --- DEĞİŞİKLİK BURADA BAŞLIYOR ---
                            .semantics { role = Role.Button } // Bu satırı ekleyin
                            .clickable(
                                // --- DEĞİŞİKLİK BURADA BİTİYOR ---
                                interactionSource = interactionSource,
                                indication = null,
                                onClick = {
                                    if (soundEnabled) soundPlayer.play("mini_click")

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
                            )
                    )
                }
            }
            if (isShowingAdLoadingIndicator) {
                val loadingMessage = stringResource(Res.string.dialog_ad_loading_video)
                Box(
                    // <-- DEĞİŞİKLİK BURADA BAŞLIYOR -->
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false, onClick = {})
                        .semantics(mergeDescendants = true) {
                            contentDescription = loadingMessage
                        },
                    // <-- DEĞİŞİKLİK BİTİYOR -->
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = ZikrTheme.colors.primary)
                }
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
fun CounterCard(
    modifier: Modifier = Modifier,
    counter: Counter,
    isSelected: Boolean,
    viewModel: AppViewModel,
    onTap: () -> Unit,
    onDelete: () -> Unit
) {
    val textColor = if (isSelected) ZikrTheme.colors.textGeneral else Color.White

    var showDeleteDialog by remember { mutableStateOf(false) }
    var showDropdownMenu by remember { mutableStateOf(false) }
    var showAddCountDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }

    val displayName = when (counter.id) {
        AppViewModel.DEFAULT_COUNTER.id -> stringResource(Res.string.default_counter_name)
        AppViewModel.NAMAZ_TESBIHATI_COUNTER.id -> stringResource(Res.string.prayer_tasbih_counter_name)
        else -> counter.name
    }

    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        if (isPressed && !showDeleteDialog && !showAddCountDialog && !showEditDialog) 0.95f else 1f,
        label = "card-scale"
    )

    val cardBackgroundColor = if (isSelected) {
        ZikrTheme.colors.secondary
    } else {
        Color.White.copy(alpha = 0.2f)
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .scale(scale)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onTap
            )
            .background(
                color = cardBackgroundColor,
                shape = RoundedCornerShape(20.dp)
            )
            .border(
                width = 1.dp,
                color = Color.White.copy(alpha = 0.3f),
                shape = RoundedCornerShape(20.dp)
            )
            .padding(14.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = displayName,
                    style = MaterialTheme.typography.titleMedium,
                    color = textColor,
                    modifier = Modifier.weight(1f),
                    overflow = TextOverflow.Ellipsis,
                    maxLines = 1
                )
                if (counter.id != AppViewModel.DEFAULT_COUNTER.id && counter.id != AppViewModel.NAMAZ_TESBIHATI_COUNTER.id) {
                    Box {
                        Image(
                            painter = painterResource(Res.drawable.ellipsis),
                            contentDescription = stringResource(Res.string.counter_card_options),
                            colorFilter = ColorFilter.tint(textColor),
                            modifier = Modifier
                                .size(24.dp)
                                .clip(CircleShape)
                                .clickable { showDropdownMenu = true }
                        )
                        MaterialTheme(
                            shapes = MaterialTheme.shapes.copy(extraSmall = RoundedCornerShape(16.dp)),
                        ) {
                            DropdownMenu(
                                expanded = showDropdownMenu,
                                onDismissRequest = { showDropdownMenu = false },
                                modifier = Modifier.clip(RoundedCornerShape(12.dp))
                            ) {
                                val isPinned = counter.pinTimestamp > 0L
                                DropdownMenuItem(
                                    text = {
                                        Text(
                                            if (isPinned) stringResource(Res.string.counter_card_unpin)
                                            else stringResource(Res.string.counter_card_pin)
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            painter = painterResource(Res.drawable.box_pin),
                                            contentDescription = null,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    },
                                    onClick = {
                                        viewModel.pinCounter(counter.id)
                                        showDropdownMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.counter_card_add_count)) },
                                    leadingIcon = { Icon(painter = painterResource(Res.drawable.box_add), contentDescription = null, modifier = Modifier.size(24.dp)) },
                                    onClick = {
                                        showAddCountDialog = true
                                        showDropdownMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.action_edit)) },
                                    leadingIcon = { Icon(painter = painterResource(Res.drawable.box_edit), contentDescription = null, modifier = Modifier.size(24.dp)) },
                                    onClick = {
                                        showEditDialog = true
                                        showDropdownMenu = false
                                    }
                                )
                                DropdownMenuItem(
                                    text = { Text(stringResource(Res.string.action_delete)) },
                                    leadingIcon = { Icon(painter = painterResource(Res.drawable.box_delete), contentDescription = null, modifier = Modifier.size(24.dp)) },
                                    onClick = {
                                        showDropdownMenu = false
                                        showDeleteDialog = true
                                    }
                                )
                            }
                        }
                    }
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val targetText = if (counter.id == AppViewModel.DEFAULT_COUNTER.id || counter.target <= 0) {
                    stringResource(Res.string.home_display_target, 0)
                } else {
                    stringResource(Res.string.home_display_target, counter.target)
                }

                Text(
                    modifier = Modifier.weight(1f),
                    text = targetText,
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Start
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.home_display_round, counter.tur),
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Start,
                )
                Text(
                    modifier = Modifier.weight(1f),
                    text = stringResource(Res.string.counter_card_count, counter.count),
                    color = textColor,
                    style = MaterialTheme.typography.labelMedium,
                    textAlign = TextAlign.Start,
                )
            }
            if (counter.id != AppViewModel.DEFAULT_COUNTER.id && counter.id != AppViewModel.NAMAZ_TESBIHATI_COUNTER.id) {
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    val formattedDateTime = rememberFormattedDateTime(counter.creationTimestamp)
                    Text(
                        text = stringResource(Res.string.counter_card_creation_date, formattedDateTime),
                        color = textColor,
                        style = MaterialTheme.typography.labelSmall,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )

                    if (counter.pinTimestamp > 0L) {
                        Image(
                            painter = painterResource(Res.drawable.box_pin),
                            contentDescription = stringResource(Res.string.counter_card_pin),
                            colorFilter = ColorFilter.tint(textColor),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }

    if (showAddCountDialog) {
        ModifyCountDialog(
            dialogTitle = stringResource(Res.string.counter_card_add_count),
            counter = counter,
            isAdding = true,
            onDismiss = { showAddCountDialog = false },
            onConfirm = { amount ->
                viewModel.modifyCounterCount(counter.id, amount)
                showAddCountDialog = false
            }
        )
    }
    if (showEditDialog) {
        CounterUpsertDialog(
            existingCounter = counter,
            onDismiss = { showEditDialog = false },
            onSave = { id, newName, newTarget ->
                id?.let {
                    viewModel.updateCounter(it, newName, newTarget)
                }
                showEditDialog = false
            }
        )
    }
    if (showDeleteDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.dialog_delete_counter_title),
            question = stringResource(Res.string.dialog_delete_counter_message, displayName),
            confirmButtonText = stringResource(Res.string.action_delete),
            onDismiss = { showDeleteDialog = false },
            onConfirm = {
                onDelete()
                showDeleteDialog = false
            }
        )
    }
}

@Composable
private fun rememberFormattedDateTime(timestamp: Long): String {
    val unknownDateText = stringResource(Res.string.common_unknown_date)

    // remember bloğu, gereksiz yeniden hesaplamayı önlemek için kalabilir.
    return remember(timestamp, unknownDateText) {
        // Artık platforma özel, yerelleştirilmiş fonksiyonu çağırıyoruz.
        // Hata kontrolü ve formatlama 'actual' tarafta yapılır.
        formatTimestampToLocalDateTime(timestamp, unknownDateText)
    }
}

@Composable
fun ModifyCountDialog(
    dialogTitle: String,
    counter: Counter,
    isAdding: Boolean,
    onDismiss: () -> Unit,
    onConfirm: (amount: Int) -> Unit
) {
    // ... fonksiyonun başındaki mantık aynı ...
    var amountString by remember { mutableStateOf("") }
    val amountInt = amountString.toIntOrNull() ?: 0
    val target = counter.target
    val finalCount: Int
    val finalTur: Int

    if (target > 0) {
        val initialTotalCount = (counter.tur * target) + counter.count
        val prospectiveTotalCount = if (isAdding) {
            initialTotalCount + amountInt
        } else {
            (initialTotalCount - amountInt).coerceAtLeast(0)
        }
        finalTur = prospectiveTotalCount / target
        finalCount = prospectiveTotalCount % target
    } else {
        finalTur = counter.tur
        finalCount = if (isAdding) {
            counter.count + amountInt
        } else {
            (counter.count - amountInt).coerceAtLeast(0)
        }
    }
    val isConfirmButtonEnabled = amountInt > 0
    val previewDescription = stringResource(Res.string.common_preview) + ": " + stringResource(Res.string.home_display_target, target) + ", " + stringResource(Res.string.home_display_round, finalTur) + ", " + stringResource(Res.string.accessibility_value) + finalCount

    var dialogWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val widthPx = coordinates.size.width
                    dialogWidth = with(density) { widthPx.toDp() }
                }
                .heightIn(min = dialogWidth * 0.4f)
                .clip(RoundedCornerShape(16.dp))
                .background(ZikrTheme.colors.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            // ... Başlık Row'u aynı ...
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZikrTheme.colors.primary)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
                Image(
                    painter = painterResource(Res.drawable.action_delete),
                    contentDescription = stringResource(Res.string.action_close),
                    modifier = Modifier
                        .size(24.dp)
                        .clickable { onDismiss() }
                )
            }
            Column(
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    // ... OutlinedTextField aynı ...
                    OutlinedTextField(
                        value = amountString,
                        onValueChange = { amountString = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(text = if (isAdding) { stringResource(Res.string.dialog_counter_add_count_label) } else { stringResource(Res.string.dialog_counter_decrease_count) }, style = MaterialTheme.typography.labelMedium )
                        },
                        textStyle = MaterialTheme.typography.titleMedium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZikrTheme.colors.primary,
                            unfocusedBorderColor = Color.Gray,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.8f)
                            .background(color = Color.DarkGray, shape = RoundedCornerShape(percent = 15))
                            .padding(top = 8.dp)
                            .semantics { contentDescription = previewDescription },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = stringResource(Res.string.common_preview),
                            style = MaterialTheme.typography.labelMedium,
                            color = ZikrTheme.colors.textOnPrimary,
                            // --- DEĞİŞİKLİK ---
                            modifier = Modifier.clearAndSetSemantics { }
                        )
                        Text(
                            text = finalCount.toString(),
                            color = ZikrTheme.colors.textOnPrimary,
                            textAlign = TextAlign.Center, fontSize = 50.sp,
                            // --- DEĞİŞİKLİK ---
                            modifier = Modifier.clearAndSetSemantics { }
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(0.95f).padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = stringResource(Res.string.home_display_target, target),
                                style = MaterialTheme.typography.labelMedium,
                                color = ZikrTheme.colors.textOnPrimary,
                                // --- DEĞİŞİKLİK ---
                                modifier = Modifier.clearAndSetSemantics { }
                            )
                            Text(
                                text = stringResource(Res.string.home_display_round, finalTur),
                                style = MaterialTheme.typography.labelMedium,
                                color = ZikrTheme.colors.textOnPrimary,
                                // --- DEĞİŞİKLİK ---
                                modifier = Modifier.clearAndSetSemantics { }
                            )
                        }
                    }
                }
                // ... DialogButtons aynı ...
                DialogButtons(
                    onConfirm = {
                        val finalAmount = if (isAdding) amountInt else -amountInt
                        onConfirm(finalAmount)
                    },
                    onDismiss = onDismiss,
                    confirmButtonText = stringResource(Res.string.common_ok),
                    isConfirmEnabled = isConfirmButtonEnabled
                )
            }
        }
    }
}

@Composable
fun CounterUpsertDialog(
    existingCounter: Counter? = null,
    onDismiss: () -> Unit,
    onSave: (id: Long?, name: String, target: String) -> Unit
) {
    var name by remember { mutableStateOf(existingCounter?.name ?: "") }
    var target by remember { mutableStateOf(existingCounter?.target?.takeIf { it > 0 }?.toString() ?: "") }
    val isSaveButtonEnabled = name.isNotBlank()
    val title = if (existingCounter != null) { stringResource(Res.string.dialog_edit_counter_title) } else { stringResource(Res.string.dialog_add_counter_title) }

    var dialogWidth by remember { mutableStateOf(0.dp) }
    val density = LocalDensity.current

    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onGloballyPositioned { coordinates ->
                    val widthPx = coordinates.size.width
                    dialogWidth = with(density) { widthPx.toDp() }
                }
                .heightIn(min = dialogWidth * 0.4f)
                .clip(RoundedCornerShape(16.dp))
                .background(ZikrTheme.colors.surface),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZikrTheme.colors.primary)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Spacer(modifier = Modifier.size(24.dp))
                Text(
                    text = title, style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = Color.Black
                )
                Image(
                    painter = painterResource(Res.drawable.action_delete),
                    contentDescription = stringResource(Res.string.action_close),
                    modifier = Modifier.size(24.dp).clickable { onDismiss() }
                )
            }
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZikrTheme.colors.surface)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(
                                text = stringResource(Res.string.dialog_counter_name_label),
                                style = MaterialTheme.typography.labelMedium
                            )
                        },

                        textStyle = MaterialTheme.typography.titleMedium,
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZikrTheme.colors.primary,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    OutlinedTextField(
                        value = target,
                        onValueChange = { target = it.filter { char -> char.isDigit() } },
                        modifier = Modifier.fillMaxWidth(),
                        label = {
                            Text(text = stringResource(Res.string.dialog_counter_target_label),
                                style = MaterialTheme.typography.labelMedium )
                        },
                        textStyle = MaterialTheme.typography.titleMedium,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        shape = RoundedCornerShape(16.dp),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = ZikrTheme.colors.primary,
                            focusedLabelColor = Color.Gray,
                            unfocusedLabelColor = Color.Gray
                        )
                    )
                }
                DialogButtons(
                    onConfirm = { onSave(existingCounter?.id, name, target) },
                    onDismiss = onDismiss,
                    confirmButtonText = stringResource(Res.string.action_save),
                    isConfirmEnabled = isSaveButtonEnabled
                )
            }
        }
    }
}