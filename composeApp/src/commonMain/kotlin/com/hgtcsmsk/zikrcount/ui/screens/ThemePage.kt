// composeApp/src/commonMain/kotlin/com/hgtcsmsk/zikrcount/ui/screens/ThemePage.kt

package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.RewardedAdState
import com.hgtcsmsk.zikrcount.platform.ShowAdResult
import com.hgtcsmsk.zikrcount.platform.SystemBackButtonHandler
import com.hgtcsmsk.zikrcount.platform.rememberAdController
import com.hgtcsmsk.zikrcount.ui.components.SuccessSnackBar
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.utils.autoMirror
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

private const val UNLOCK_TYPE_BACKGROUND = "bg"
private const val UNLOCK_TYPE_THEME = "theme"

private enum class ThemeTab(val index: Int) { Display(0), Background(1) }

data class ColorTheme(
    val name: String,
    val primary: Color,
    val secondary: Color
)

private fun getColorThemes(): List<ColorTheme> {
    return listOf(
        ColorTheme("sunshine",      primary = Color(0xFFfcc95b), secondary = Color(0xFFfff2d5)),
        ColorTheme("blueTurquoise", primary = Color(0xFF00cef7), secondary = Color(0xFFCCF5FE)),
        ColorTheme("deepMagenta",   primary = Color(0xFFe1adec), secondary = Color(0xFFF4DCE9)),
        ColorTheme("mulberryWine",  primary = Color(0xFFffa8e8), secondary = Color(0xFFffedfc)),
        ColorTheme("sunsetCoral",   primary = Color(0xFFff6f61), secondary = Color(0xFFffe7e3)),
        ColorTheme("burntLime",     primary = Color(0xFF7ed58c), secondary = Color(0xFFe9fcde)),
        ColorTheme("lightGreen",    primary = Color(0xFFe4de64), secondary = Color(0xFFf2efb9)),
        ColorTheme("cobaltSky",     primary = Color(0xFF94a6d4), secondary = Color(0xFFd0ddfd)),
    )
}

// HATA DÜZELTMESİ: Gereksiz @Composable etiketi kaldırıldı.
fun findColorThemeByName(name: String): ColorTheme {
    return getColorThemes().find { it.name == name } ?: getColorThemes().first()
}

private val defaultUnlockedBackgrounds = setOf("background_1", "background_2")
private val defaultUnlockedThemes = setOf("sunshine", "blueTurquoise")

@Composable
fun ThemePage(viewModel: AppViewModel, onNavigateBack: () -> Unit) {
    DisposableEffect(Unit) {
        onDispose {
            viewModel.resetAdFailureCount()
        }
    }

    SystemBackButtonHandler { onNavigateBack() }
    val pagerState = rememberPagerState(pageCount = { 2 })
    val scope = rememberCoroutineScope()
    val currentTab = ThemeTab.entries.getOrNull(pagerState.currentPage) ?: ThemeTab.Display

    val selectedBackgroundName by viewModel.selectedBackground.collectAsState()
    val selectedThemeName by viewModel.selectedDisplayTheme.collectAsState()
    val unlockedItems by viewModel.unlockedItems.collectAsState()
    val selectedColorTheme = findColorThemeByName(selectedThemeName)
    val purchaseState by viewModel.purchaseState.collectAsState()

    // HATA DÜZELTMESİ: `remember` içinden çağrılacak `getBackgroundResources` fonksiyonu
    // @Composable olmamalı, bu nedenle `remember` bloğu artık hata vermeyecek.
    val allBackgrounds = remember { getBackgroundResources() }
    val allColorThemes = remember { getColorThemes() }

    var showAdDialog by remember { mutableStateOf(false) }
    var itemToUnlock by remember { mutableStateOf<Pair<String, String>?>(null) }
    var showNoInternetDialog by remember { mutableStateOf(false) }
    var showAdLoadErrorDialog by remember { mutableStateOf(false) }
    var showGiftDialog by remember { mutableStateOf(false) }
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val adFailureCount by viewModel.adFailureCount.collectAsState()
    val isShowingAdLoadingIndicator by viewModel.isShowingAdLoadingIndicator.collectAsState()
    val adRetryTrigger by viewModel.adRetryTrigger.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val unlockSuccessMessage = stringResource(Res.string.theme_page_unlock_success)

    val adController = rememberAdController(
        viewModel = viewModel,
        retryTrigger = adRetryTrigger,
        onAdFailedToLoad = { errorMsg ->
            println("Silent Ad Load Failed on ThemePage: $errorMsg")
        }
    )

    LaunchedEffect(adController.adState) {
        if (adController.adState == RewardedAdState.NOT_LOADED) {
            adController.loadRewardAd()
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                SuccessSnackBar(data = data)
            }
        },
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(findBackgroundResource(selectedBackgroundName)),
                contentDescription = null, // Dekoratif resim, okunmamalı
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.85f))
            )

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(8.dp),
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
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = null,
                                onClick = { onNavigateBack() }
                            )
                            .autoMirror()
                    )
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            contentAlignment = Alignment.CenterEnd,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(ThemeTab.Display.index) } }
                                )
                                .padding(horizontal = 6.dp)
                                .semantics {
                                    this.selected = currentTab == ThemeTab.Display
                                }
                        ) {
                            val isSelected = currentTab == ThemeTab.Display
                            Text(
                                text = stringResource(Res.string.theme_page_tab_display),
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W500,
                                modifier = (if (isSelected) {
                                    Modifier.background(selectedColorTheme.primary, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }).padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                        Box(
                            contentAlignment = Alignment.CenterStart,
                            modifier = Modifier
                                .weight(1f)
                                .clickable(
                                    interactionSource = remember { MutableInteractionSource() },
                                    indication = null,
                                    onClick = { scope.launch { pagerState.animateScrollToPage(ThemeTab.Background.index) } }
                                )
                                .padding(horizontal = 6.dp)
                                .semantics {
                                    this.selected = currentTab == ThemeTab.Background
                                }
                        ) {
                            val isSelected = currentTab == ThemeTab.Background
                            Text(
                                text = stringResource(Res.string.theme_page_tab_background),
                                color = if (isSelected) Color.Black else Color.White,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.W500,
                                modifier = (if (isSelected) {
                                    Modifier.background(selectedColorTheme.primary, RoundedCornerShape(8.dp))
                                } else {
                                    Modifier
                                }).padding(horizontal = 12.dp, vertical = 4.dp)
                            )
                        }
                    }
                    Spacer(modifier = Modifier.size(32.dp))
                }

                HorizontalPager(
                    state = pagerState,
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                ) { page ->
                    when (ThemeTab.entries.getOrNull(page)) {
                        ThemeTab.Display -> DisplayThemesGrid(
                            allThemes = allColorThemes,
                            selectedThemeName = selectedThemeName,
                            unlockedItems = unlockedItems,
                            purchaseState = purchaseState,
                            onSelect = { viewModel.setSelectedDisplayTheme(it) },
                            onUnlockRequest = { themeName ->
                                if (!isNetworkAvailable) {
                                    showNoInternetDialog = true
                                } else {
                                    itemToUnlock = Pair(UNLOCK_TYPE_THEME, themeName)
                                    showAdDialog = true
                                }
                            }
                        )

                        ThemeTab.Background -> BackgroundsGrid(
                            allBackgrounds = allBackgrounds,
                            selectedBackgroundName = selectedBackgroundName,
                            selectedColorTheme = selectedColorTheme,
                            unlockedItems = unlockedItems,
                            purchaseState = purchaseState,
                            onSelect = { viewModel.setSelectedBackground(it) },
                            onUnlockRequest = { backgroundName ->
                                if (!isNetworkAvailable) {
                                    showNoInternetDialog = true
                                } else {
                                    itemToUnlock = Pair(UNLOCK_TYPE_BACKGROUND, backgroundName)
                                    showAdDialog = true
                                }
                            }
                        )
                        null -> {}
                    }
                }
            }
            if (isShowingAdLoadingIndicator) {
                val loadingMessage = stringResource(Res.string.theme_page_loading_ad)
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                        .clickable(enabled = false, onClick = {})
                        .semantics(mergeDescendants = true) {
                            contentDescription = loadingMessage
                        },
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = selectedColorTheme.primary)
                }
            }
        }
    }

    if (showAdDialog) {
        ConfirmationDialog(
            title = stringResource(Res.string.action_watch_ad),
            question = stringResource(Res.string.dialog_ad_unlock_item_message),
            confirmButtonText = stringResource(Res.string.action_watch_ad),
            onDismiss = { showAdDialog = false },
            onConfirm = {
                showAdDialog = false
                scope.launch {
                    val adResultHandler: (ShowAdResult) -> Unit = { result ->
                        when (result) {
                            is ShowAdResult.EarnedReward -> {
                                if (result.earned) {
                                    itemToUnlock?.let { (type, name) ->
                                        val unlockKey = "$type-$name"
                                        viewModel.unlockItem(unlockKey)
                                        if (type == UNLOCK_TYPE_BACKGROUND) {
                                            viewModel.setSelectedBackground(name)
                                        } else {
                                            viewModel.setSelectedDisplayTheme(name)
                                        }
                                        scope.launch {
                                            snackbarHostState.showSnackbar(unlockSuccessMessage)
                                        }
                                    }
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
                itemToUnlock?.let { (type, name) ->
                    val unlockKey = "$type-$name"
                    viewModel.unlockItem(unlockKey)
                    if (type == UNLOCK_TYPE_BACKGROUND) {
                        viewModel.setSelectedBackground(name)
                    } else {
                        viewModel.setSelectedDisplayTheme(name)
                    }
                }
                showGiftDialog = false
            }
        )
    }
}

@Composable
private fun BackgroundsGrid(
    allBackgrounds: List<Pair<String, DrawableResource>>,
    selectedBackgroundName: String,
    selectedColorTheme: ColorTheme,
    unlockedItems: Set<String>,
    purchaseState: PurchaseState,
    onSelect: (String) -> Unit,
    onUnlockRequest: (String) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        itemsIndexed(allBackgrounds, key = { _, item -> item.first }) { index, (name, resource) ->
            val isSelected = selectedBackgroundName == name
            val isUnlocked = unlockedItems.contains("$UNLOCK_TYPE_BACKGROUND-$name") ||
                    defaultUnlockedBackgrounds.contains(name) ||
                    purchaseState is PurchaseState.Purchased

            val baseModifier = if (isSelected) {
                val selectionGlowColor = selectedColorTheme.primary
                Modifier
                    .shadow(
                        elevation = 8.dp,
                        shape = RoundedCornerShape(20.dp),
                        ambientColor = selectionGlowColor,
                        spotColor = selectionGlowColor
                    )
                    .border(
                        width = 2.dp,
                        color = selectionGlowColor,
                        shape = RoundedCornerShape(20.dp)
                    )
            } else {
                Modifier
            }

            val stateText = when {
                isSelected -> stringResource(Res.string.theme_preview_state_selected)
                !isUnlocked -> stringResource(Res.string.theme_preview_state_locked)
                else -> ""
            }
            val description = stringResource(Res.string.theme_preview_background_desc, index + 1, stateText)

            Box(
                modifier = baseModifier
                    .clickable {
                        if (isUnlocked) {
                            onSelect(name)
                        } else {
                            onUnlockRequest(name)
                        }
                    }
                    .semantics { contentDescription = description }
            ) {
                BackgroundPreview(
                    backgroundResource = resource,
                    isLocked = !isUnlocked
                )
            }
        }
    }
}

@Composable
private fun DisplayThemesGrid(
    allThemes: List<ColorTheme>,
    selectedThemeName: String,
    unlockedItems: Set<String>,
    purchaseState: PurchaseState,
    onSelect: (String) -> Unit,
    onUnlockRequest: (String) -> Unit
) {
    LazyVerticalGrid(
        modifier = Modifier.fillMaxSize(),
        columns = GridCells.Fixed(2),
        contentPadding = PaddingValues(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        itemsIndexed(allThemes, key = { _, theme -> theme.name }) { index, theme ->
            val isSelected = selectedThemeName == theme.name
            val isUnlocked = unlockedItems.contains("$UNLOCK_TYPE_THEME-${theme.name}") ||
                    defaultUnlockedThemes.contains(theme.name) ||
                    purchaseState is PurchaseState.Purchased

            val baseModifier = if (isSelected) {
                Modifier
                    .border(
                        width = 2.dp,
                        color = theme.secondary,
                        shape = RoundedCornerShape(20.dp)
                    )
            } else {
                Modifier
            }

            val stateText = when {
                isSelected -> stringResource(Res.string.theme_preview_state_selected)
                !isUnlocked -> stringResource(Res.string.theme_preview_state_locked)
                else -> ""
            }
            val description = stringResource(Res.string.theme_preview_theme_desc, index + 1, stateText)

            Box(
                modifier = baseModifier
                    .clickable {
                        if (isUnlocked) {
                            onSelect(theme.name)
                        } else {
                            onUnlockRequest(theme.name)
                        }
                    }
                    .semantics { contentDescription = description }
            ) {
                LayeredThemePreview(
                    primaryColor = theme.primary,
                    isLocked = !isUnlocked,
                    isSelected = isSelected
                )
            }
        }
    }
}

@Composable
private fun BackgroundPreview(
    modifier: Modifier = Modifier,
    backgroundResource: DrawableResource,
    isLocked: Boolean
) {
    Box(
        modifier = modifier
            .aspectRatio(9f / 18f)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Image(
            painter = painterResource(backgroundResource),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop
        )

        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.lock),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}

@Composable
private fun LayeredThemePreview(
    modifier: Modifier = Modifier,
    primaryColor: Color,
    isLocked: Boolean,
    isSelected: Boolean
) {
    val cardBackgroundColor = if (isSelected) {
        primaryColor.copy(alpha = 0.5f)
    } else {
        Color.White.copy(alpha = 0.15f)
    }

    Box(
        modifier = modifier
            .aspectRatio(9f / 18f)
            .clip(RoundedCornerShape(20.dp))
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = cardBackgroundColor,
                    shape = RoundedCornerShape(20.dp)
                )
                .clip(RoundedCornerShape(20.dp))
        ) {
            Image(
                painter = painterResource(Res.drawable.vitrual_back),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                colorFilter = ColorFilter.tint(primaryColor)
            )
            Image(
                painter = painterResource(Res.drawable.vitrual_front),
                contentDescription = null,
                modifier = Modifier.fillMaxSize()
            )
        }

        if (isLocked) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(12.dp)
                    .background(Color.Black.copy(alpha = 0.5f), CircleShape)
                    .padding(4.dp)
            ) {
                Image(
                    painter = painterResource(Res.drawable.lock),
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }
    }
}

// HATA DÜZELTMESİ: Gereksiz @Composable etiketi kaldırıldı.
fun findBackgroundResource(name: String): DrawableResource {
    return getBackgroundResources().find { it.first == name }?.second ?: Res.drawable.background_2
}

// HATA DÜZELTMESİ: Gereksiz @Composable etiketi kaldırıldı.
private fun getBackgroundResources(): List<Pair<String, DrawableResource>> {
    return listOf(
        "background_1" to Res.drawable.background_1,
        "background_2" to Res.drawable.background_2,
        "background_3" to Res.drawable.background_3,
        "background_4" to Res.drawable.background_4,
        "background_5" to Res.drawable.background_5,
        "background_6" to Res.drawable.background_6,
        "background_7" to Res.drawable.background_7,
        "background_8" to Res.drawable.background_8,
        "background_9" to Res.drawable.background_9,
        "background_10" to Res.drawable.background_10,
        "background_11" to Res.drawable.background_11,
        "background_12" to Res.drawable.background_12,
        "background_13" to Res.drawable.background_13,
        "background_14" to Res.drawable.background_14,
        "background_15" to Res.drawable.background_15,
        "background_16" to Res.drawable.background_16,
        "background_17" to Res.drawable.background_17,
        "background_18" to Res.drawable.background_18,
        "background_19" to Res.drawable.background_19,
        "background_20" to Res.drawable.background_20,
        "background_21" to Res.drawable.background_21,
        "background_22" to Res.drawable.background_22,
    )
}