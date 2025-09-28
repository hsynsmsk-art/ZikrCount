// composeApp/src/commonMain/kotlin/com/hgtcsmsk/zikrcount/ui/screens/TabletLayout.kt

package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.platform.BannerAd
import com.hgtcsmsk.zikrcount.platform.PlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.SoundPlayer
import com.hgtcsmsk.zikrcount.ui.components.SuccessSnackBar
import com.hgtcsmsk.zikrcount.ui.components.TabletActionButton
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

@Composable
fun TabletLayout(
    viewModel: AppViewModel,
    platformActionHandler: PlatformActionHandler,
    soundPlayer: SoundPlayer,
    onNavigateToTheme: () -> Unit,
    onNavigateToSettings: () -> Unit
) {
    val selectedBackground by viewModel.selectedBackground.collectAsState()
    val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
    val showBackupDialog by viewModel.showBackupConfirmationDialog.collectAsState()
    val snackBarHostState = remember { SnackbarHostState() }
    var isDualPaneVisible by remember { mutableStateOf(true) }
    val bannerAdTrigger = 0
    val isNetworkAvailable by viewModel.isNetworkAvailable.collectAsState()
    val isAdPlaying by viewModel.isAdPlaying.collectAsState()

    // --> YENİ: Bildirim ikonunu göstermek için ViewModel'den durumu dinliyoruz.
    val showUpdateBadge by viewModel.showUpdateBadge.collectAsState()

    var rememberedStatusBarHeight by remember { mutableStateOf(0.dp) }
    val currentStatusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    if (currentStatusBarHeight > 0.dp) {
        rememberedStatusBarHeight = currentStatusBarHeight
    }

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

        Scaffold(
            contentWindowInsets = WindowInsets(0.dp),
            containerColor = Color.Transparent,
            snackbarHost = {
                SnackbarHost(
                    hostState = snackBarHostState,
                    modifier = Modifier.align(Alignment.BottomCenter)
                        .navigationBarsPadding()
                        .padding(bottom = 80.dp, start = 24.dp, end = 24.dp),
                    snackbar = { data -> SuccessSnackBar(data = data) }
                )
            }
        ) { innerPadding ->
            Column(modifier = Modifier.fillMaxSize()) {
                Spacer(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(rememberedStatusBarHeight)
                        .background(if (isAdPlaying) Color.Black else Color.Transparent)
                )

                BoxWithConstraints(
                    modifier = Modifier
                        .weight(1f)
                        .padding(innerPadding)
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                ) {
                    val animationSpecFloat = tween<Float>(durationMillis = 600)
                    val animationSpecDp = tween<Dp>(durationMillis = 600)

                    val rotationAngle by animateFloatAsState(
                        targetValue = if (isDualPaneVisible) 0f else 180f,
                        animationSpec = animationSpecFloat, label = "rotation"
                    )

                    val leftPaneAlpha by animateFloatAsState(
                        targetValue = if (isDualPaneVisible) 1f else 0f,
                        animationSpec = tween(durationMillis = 400), label = "leftPaneAlpha"
                    )

                    val paneWidth = this.maxWidth * 0.45f
                    val offsetValue = (paneWidth / 2) + 8.dp

                    val paneOffset by animateDpAsState(
                        targetValue = if (isDualPaneVisible) offsetValue else 0.dp,
                        animationSpec = animationSpecDp, label = "paneOffset"
                    )

                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        if (leftPaneAlpha > 0f) {
                            Box(
                                modifier = Modifier
                                    .offset(x = -paneOffset)
                                    .fillMaxWidth(0.45f)
                                    .aspectRatio(9f / 18f)
                                    .graphicsLayer { alpha = leftPaneAlpha }
                            ) {
                                TabletCountersPageContent(viewModel, soundPlayer, snackBarHostState)
                            }
                        }

                        Box(
                            modifier = Modifier
                                .offset(x = paneOffset)
                                .fillMaxWidth(0.45f)
                                .aspectRatio(9f / 18f)
                        ) {
                            TabletHomePageContent(
                                viewModel,
                                platformActionHandler,
                                soundPlayer,
                                snackBarHostState,
                                rotationAngle = rotationAngle,
                                onToggle = { isDualPaneVisible = !isDualPaneVisible }
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.TopCenter)
                            .padding(top = 12.dp)
                    ) {
                        TabletActionButton(
                            iconResource = Res.drawable.brush,
                            contentDescription = stringResource(Res.string.content_desc_theme_button),
                            onClick = onNavigateToTheme,
                            modifier = Modifier.align(Alignment.TopStart).size(44.dp)
                        )

                        // --> YENİ: Tabletteki ayarlar butonu da artık bir Box içinde.
                        Box(
                            modifier = Modifier.align(Alignment.TopEnd),
                            contentAlignment = Alignment.TopEnd
                        ) {
                            TabletActionButton(
                                iconResource = Res.drawable.setting,
                                contentDescription = stringResource(Res.string.content_desc_settings_button),
                                onClick = onNavigateToSettings,
                                modifier = Modifier.size(44.dp)
                            )
                            if (showUpdateBadge) {
                                Box(
                                    modifier = Modifier
                                        .padding(1.dp)
                                        .size(12.dp)
                                        .background(Color.Red, CircleShape)
                                        .border(1.dp, Color.White, CircleShape)
                                )
                            }
                        }
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .navigationBarsPadding(),
            contentAlignment = Alignment.Center
        ) {
            if (isNetworkAvailable) {
                BannerAd(
                    modifier = Modifier.fillMaxWidth(),
                    trigger = bannerAdTrigger
                )
            }
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
    }
}