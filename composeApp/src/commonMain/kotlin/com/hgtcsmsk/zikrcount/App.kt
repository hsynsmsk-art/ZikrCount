package com.hgtcsmsk.zikrcount

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowHeightSizeClass
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.hgtcsmsk.zikrcount.data.UpdateState
import com.hgtcsmsk.zikrcount.platform.rememberPlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.rememberSoundPlayer
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.dialog.UpdateDialog
import com.hgtcsmsk.zikrcount.ui.screens.*
import com.hgtcsmsk.zikrcount.ui.theme.DefaultTypography
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.theme.withAdjustedFontSizes
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

enum class Screen {
    Home, Counters, Theme, Settings, RemoveAds
}

@Composable
fun App(windowSizeClass: WindowSizeClass, viewModel: AppViewModel) {
    var currentScreen by remember { mutableStateOf(Screen.Home) }
    val navigateTo = { nextScreen: Screen -> currentScreen = nextScreen }

    val platformActionHandler = rememberPlatformActionHandler()
    val soundPlayer = rememberSoundPlayer()

    val selectedThemeName by viewModel.selectedDisplayTheme.collectAsState()
    val colorTheme = findColorThemeByName(selectedThemeName)

    val typographyToUse = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> DefaultTypography.withAdjustedFontSizes(4)
        WindowWidthSizeClass.Medium -> DefaultTypography.withAdjustedFontSizes(2)
        else -> DefaultTypography
    }

    val updateState by viewModel.updateState.collectAsState()
    val shouldShowTalkbackPrompt by viewModel.shouldShowTalkbackPrompt.collectAsState()

    ZikrTheme(
        primaryColor = colorTheme.primary,
        secondaryColor = colorTheme.secondary
    ) {
        MaterialTheme(
            colorScheme = lightColorScheme(
                primary = colorTheme.primary,
                secondary = colorTheme.secondary
            ),
            typography = typographyToUse
        ) {
            val isTabletOrLandscape = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact ||
                    windowSizeClass.heightSizeClass == WindowHeightSizeClass.Compact

            Box(modifier = Modifier.fillMaxSize()) {
                Crossfade(
                    targetState = currentScreen,
                    modifier = Modifier.fillMaxSize(),
                    animationSpec = tween(durationMillis = 300),
                    label = "screen_crossfade"
                ) { screen ->
                    when (screen) {
                        Screen.Home -> {
                            if (isTabletOrLandscape) {
                                TabletLayout(
                                    viewModel = viewModel,
                                    platformActionHandler = platformActionHandler,
                                    soundPlayer = soundPlayer,
                                    onNavigateToTheme = { navigateTo(Screen.Theme) },
                                    onNavigateToSettings = { navigateTo(Screen.Settings) },
                                    windowSizeClass = windowSizeClass
                                )
                            } else {
                                HomePage(
                                    viewModel = viewModel,
                                    onNavigateToCounters = { navigateTo(Screen.Counters) },
                                    onNavigateToTheme = { navigateTo(Screen.Theme) },
                                    onNavigateToSettings = { navigateTo(Screen.Settings) },
                                    platformActionHandler = platformActionHandler,
                                    soundPlayer = soundPlayer,
                                    windowSizeClass = windowSizeClass
                                )
                            }
                        }
                        Screen.Counters -> CountersPage(
                            viewModel = viewModel,
                            onNavigateBack = { navigateTo(Screen.Home) },
                            soundPlayer = soundPlayer
                        )
                        Screen.Theme -> ThemePage(
                            viewModel = viewModel,
                            onNavigateBack = { navigateTo(Screen.Home) }
                        )
                        Screen.Settings -> SettingsPage(
                            viewModel = viewModel,
                            onNavigateBack = { navigateTo(Screen.Home) },
                            platformActionHandler = platformActionHandler,
                            onNavigateToPremium = { navigateTo(Screen.RemoveAds) }
                        )
                        Screen.RemoveAds -> PremiumPage(
                            viewModel = viewModel,
                            onNavigateBack = { navigateTo(Screen.Settings) }
                        )
                    }
                }

                if (shouldShowTalkbackPrompt) {
                    ConfirmationDialog(
                        title = stringResource(Res.string.dialog_talkback_title),
                        question = stringResource(Res.string.dialog_talkback_message),
                        confirmButtonText = stringResource(Res.string.action_yes_enable),
                        onConfirm = { viewModel.onTalkbackPromptResult(isAccepted = true) },
                        onDismiss = { viewModel.onTalkbackPromptResult(isAccepted = false) }
                    )
                }

                if (updateState is UpdateState.Mandatory) {
                    UpdateDialog(
                        updateState = updateState,
                        onConfirm = {
                            platformActionHandler.openAppStore()
                        },
                        onDismiss = {
                        }
                    )
                }
            }
        }
    }
}