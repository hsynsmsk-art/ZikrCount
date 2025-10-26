package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.platform.BannerAd
import com.hgtcsmsk.zikrcount.platform.PlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.SoundPlayer
import com.hgtcsmsk.zikrcount.ui.components.SuccessSnackBar
import com.hgtcsmsk.zikrcount.ui.theme.withAdjustedFontSizes
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*
import kotlin.math.max

@Composable
fun TabletLayout(
    viewModel: AppViewModel,
    platformActionHandler: PlatformActionHandler,
    soundPlayer: SoundPlayer,
    onNavigateToTheme: () -> Unit,
    onNavigateToSettings: () -> Unit,
    windowSizeClass: WindowSizeClass
) {
    val isPhoneLandscape = windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact
    val localTypography = if (isPhoneLandscape) {
        MaterialTheme.typography.withAdjustedFontSizes(-2)
    } else {
        MaterialTheme.typography
    }

    MaterialTheme(typography = localTypography) {
        val selectedBackground by viewModel.selectedBackground.collectAsState()
        val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
        val snackBarHostState = remember { SnackbarHostState() }
        LaunchedEffect(key1 = Unit) {
            viewModel.eventFlow.collect { event ->
                when (event) {
                    is AppViewModel.UiEvent.ShowSnackbar -> {
                        snackBarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
        var isDualPaneVisible by remember { mutableStateOf(true) }
        val bannerAdTrigger = 0
        val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
        val purchaseState by viewModel.purchaseState.collectAsState()

        BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
            val isLandscape = maxWidth > maxHeight

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

            Scaffold(
                containerColor = Color.Transparent,
                snackbarHost = {
                    SnackbarHost(
                        hostState = snackBarHostState,
                        modifier = Modifier.windowInsetsPadding(
                            WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)
                        )
                    ) { data ->
                        SuccessSnackBar(data = data)
                    }
                },
                bottomBar = {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .windowInsetsPadding(WindowInsets.navigationBars.only(WindowInsetsSides.Bottom + WindowInsetsSides.Horizontal)),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isNetworkAvailable && purchaseState !is PurchaseState.Purchased) {
                            val bannerHeight = if (isPhoneLandscape) 60.dp else 90.dp
                            BannerAd(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(bannerHeight),
                                trigger = bannerAdTrigger
                            )
                        } else {
                            Spacer(Modifier.height(0.dp))
                        }
                    }
                }
            ) { innerPadding ->
                val layoutDirection = LocalLayoutDirection.current
                val contentPadding = if (isLandscape && isPhoneLandscape) {
                    val startPadding = innerPadding.calculateStartPadding(layoutDirection)
                    val endPadding = innerPadding.calculateEndPadding(layoutDirection)
                    val maxHorizontalPadding = remember(startPadding, endPadding) { max(startPadding.value, endPadding.value) }.dp

                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = maxHorizontalPadding,
                        end = maxHorizontalPadding,
                        bottom = innerPadding.calculateBottomPadding()
                    )
                } else {
                    PaddingValues(
                        top = innerPadding.calculateTopPadding(),
                        start = innerPadding.calculateStartPadding(layoutDirection),
                        end = innerPadding.calculateEndPadding(layoutDirection),
                        bottom = innerPadding.calculateBottomPadding()
                    )
                }


                BoxWithConstraints(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(contentPadding)
                ) {
                    val currentMaxWidth = maxWidth
                    val paneModifier = if (isLandscape) Modifier.fillMaxHeight() else Modifier.aspectRatio(9f / 18f)

                    val animationSpecFloat = tween<Float>(durationMillis = 600)
                    val animationSpecDp = tween<Dp>(durationMillis = 600)
                    val rotationAngle by animateFloatAsState(targetValue = if (isDualPaneVisible) 0f else 360f, animationSpec = animationSpecFloat, label = "rotation")
                    val leftPaneAlpha by animateFloatAsState(targetValue = if (isDualPaneVisible) 1f else 0f, animationSpec = tween(durationMillis = 400), label = "leftPaneAlpha")

                    val paneWidth = currentMaxWidth * 0.45f
                    val offsetValue = (paneWidth / 2) + 8.dp
                    val paneOffset by animateDpAsState(targetValue = if (isDualPaneVisible) offsetValue else 0.dp, animationSpec = animationSpecDp, label = "paneOffset")

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        if (leftPaneAlpha > 0f) {
                            Box(
                                modifier = Modifier.offset(x = -paneOffset).fillMaxWidth(0.45f).then(paneModifier).graphicsLayer { alpha = leftPaneAlpha }
                            ) {
                                TabletCountersPageContent(viewModel, soundPlayer, snackBarHostState, isLandscape = isLandscape)
                            }
                        }

                        Box(
                            modifier = Modifier.offset(x = paneOffset).fillMaxWidth(0.45f).then(paneModifier)
                        ) {
                            TabletHomePageContent(
                                viewModel = viewModel,
                                platformActionHandler = platformActionHandler,
                                soundPlayer = soundPlayer,
                                snackBarHostState = snackBarHostState,
                                rotationAngle = rotationAngle,
                                onToggle = { isDualPaneVisible = !isDualPaneVisible },
                                onNavigateToTheme = onNavigateToTheme,
                                onNavigateToSettings = onNavigateToSettings,
                                isLandscape = isLandscape,
                                isTablet = !isPhoneLandscape
                            )
                        }
                    }
                }
            }
        }
    }
}