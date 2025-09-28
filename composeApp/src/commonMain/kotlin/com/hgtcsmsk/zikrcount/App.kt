// composeApp/src/commonMain/kotlin/com/hgtcsmsk/zikrcount/App.kt

package com.hgtcsmsk.zikrcount

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.sp
import com.hgtcsmsk.zikrcount.data.UpdateState
import com.hgtcsmsk.zikrcount.platform.rememberPlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.rememberSoundPlayer
import com.hgtcsmsk.zikrcount.ui.dialog.UpdateDialog
import com.hgtcsmsk.zikrcount.ui.screens.*
import com.hgtcsmsk.zikrcount.ui.theme.DefaultTypography
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme

enum class Screen {
    Home, Counters, Theme, Settings, RemoveAds
}

@Composable
fun App(windowSizeClass: WindowSizeClass, viewModel: AppViewModel) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    val navigateTo = { nextScreen: Screen -> currentScreen = nextScreen }

    val platformActionHandler = rememberPlatformActionHandler()
    val soundPlayer = rememberSoundPlayer()

    val selectedThemeName by viewModel.selectedDisplayTheme.collectAsState()
    val colorTheme = findColorThemeByName(selectedThemeName)

    val typographyToUse = when (windowSizeClass.widthSizeClass) {
        WindowWidthSizeClass.Expanded -> DefaultTypography.withAdjustedFontSizes(6)
        WindowWidthSizeClass.Medium -> DefaultTypography.withAdjustedFontSizes(3)
        else -> DefaultTypography
    }

    val isTablet = windowSizeClass.widthSizeClass > WindowWidthSizeClass.Compact

    val updateState by viewModel.updateState.collectAsState()

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
            if (isTablet) {
                TabletLayout(
                    viewModel = viewModel,
                    platformActionHandler = platformActionHandler,
                    soundPlayer = soundPlayer,
                    onNavigateToTheme = { /* Tablette bu, modal bir sayfa olabilir */ },
                    onNavigateToSettings = { /* Tablette bu, modal bir sayfa olabilir */ }
                )
            } else {
                Crossfade(
                    targetState = currentScreen,
                    animationSpec = tween(durationMillis = 300),
                    label = "screen_crossfade"
                ) { screen ->
                    when (screen) {
                        Screen.Home -> HomePage(
                            viewModel = viewModel,
                            onNavigateToCounters = { navigateTo(Screen.Counters) },
                            onNavigateToTheme = { navigateTo(Screen.Theme) },
                            onNavigateToSettings = { navigateTo(Screen.Settings) },
                            platformActionHandler = platformActionHandler,
                            soundPlayer = soundPlayer
                        )

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
            }

            if (updateState is UpdateState.Mandatory) {
                UpdateDialog(
                    updateState = updateState,
                    onConfirm = {
                        platformActionHandler.openAppStore()
                    },
                    onDismiss = {
                        viewModel.dismissUpdateDialog()
                    }
                )
            }
        }
    }
}

private fun Typography.withAdjustedFontSizes(amount: Int): Typography {
    return this.copy(
        bodyLarge = this.bodyLarge.copy(fontSize = (this.bodyLarge.fontSize.value + amount).sp),
        bodyMedium = this.bodyMedium.copy(fontSize = (this.bodyMedium.fontSize.value + amount).sp),
        bodySmall = this.bodySmall.copy(fontSize = (this.bodySmall.fontSize.value + amount).sp),
        titleLarge = this.titleLarge.copy(fontSize = (this.titleLarge.fontSize.value + amount).sp),
        titleMedium = this.titleMedium.copy(fontSize = (this.titleMedium.fontSize.value + amount).sp),
        titleSmall = this.titleSmall.copy(fontSize = (this.titleSmall.fontSize.value + amount).sp)
    )
}