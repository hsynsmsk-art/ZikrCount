package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.data.UpdateState
import com.hgtcsmsk.zikrcount.platform.PlatformActionHandler
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.SystemBackButtonHandler
import com.hgtcsmsk.zikrcount.ui.dialog.ConfirmationDialog
import com.hgtcsmsk.zikrcount.ui.dialog.TtsSelectionDialog
import com.hgtcsmsk.zikrcount.ui.dialog.UpdateDialog
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.utils.autoMirror
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsPage(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
    platformActionHandler: PlatformActionHandler,
    onNavigateToPremium: () -> Unit
) {
    SystemBackButtonHandler { onNavigateBack() }

    val showUpdateBadge by viewModel.showUpdateBadge.collectAsState()
    val updateState by viewModel.updateState.collectAsState()
    var showUpdateDialog by remember { mutableStateOf(false) }

    val purchaseState by viewModel.purchaseState.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.loadTtsEngines()
    }

    val ttsEngines by viewModel.ttsEngines.collectAsState()
    val availableLanguages by viewModel.availableTtsLanguages.collectAsState()
    val hasTtsEngines = ttsEngines.isNotEmpty()
    val soundEnabled by viewModel.soundEnabled.collectAsState()
    val vibrationEnabled by viewModel.vibrationEnabled.collectAsState()
    val isNightModeEnabled by viewModel.isNightModeEnabled.collectAsState()
    val isFullScreenTouchEnabled by viewModel.isFullScreenTouchEnabled.collectAsState()
    val isBackupEnabled by viewModel.isBackupEnabled.collectAsState()
    val selectedBackground by viewModel.selectedBackground.collectAsState()
    val showBackupDialog by viewModel.showBackupConfirmationDialog.collectAsState()
    val isCounterReadingEnabled by viewModel.isCounterReadingEnabled.collectAsState()
    val ttsSpeechRate by viewModel.ttsSpeechRate.collectAsState()
    val selectedTtsEngine by viewModel.selectedTtsEngine.collectAsState()
    val selectedTtsLanguage by viewModel.selectedTtsLanguage.collectAsState()
    val isLanguageLoading by viewModel.isLanguageLoading.collectAsState()
    var showTtsEngineDialog by remember { mutableStateOf(false) }
    var showTtsLangDialog by remember { mutableStateOf(false) }
    var showNoTtsEngineDialog by remember { mutableStateOf(false) }
    val isHuaweiEngine = selectedTtsEngine.contains("huawei", ignoreCase = true)


    Scaffold(
        containerColor = Color.Transparent,
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize()) {
            Image(
                painter = painterResource(findBackgroundResource(selectedBackground)),
                contentDescription = "Ayarlar Arka Planı",
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
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
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
                                onClick = onNavigateBack
                            )
                            .autoMirror()
                    )
                    Text(
                        text = stringResource(Res.string.settings_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZikrTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(32.dp))
                }


                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(12.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    item(key = "update_banner") {
                        if (showUpdateBadge && updateState is UpdateState.Optional) {
                            UpdateAvailableBanner(
                                onClick = {
                                    viewModel.onUpdateBannerClicked()
                                    showUpdateDialog = true
                                }
                            )
                        }
                    }

                    item(key = "ads_card") {
                        SettingsCard(title = "Reklamlar") {
                            val isPurchased = purchaseState is PurchaseState.Purchased
                            if (isPurchased) {
                                SettingsRowInfo(
                                    icon = Res.drawable.no_ads,
                                    text = "Reklamlar Kaldırıldı",
                                    subText = "Uygulamayı reklamsız kullanıyorsunuz."
                                )
                            } else {
                                SettingsRowClickable(
                                    icon = Res.drawable.no_ads,
                                    text = "Reklamları Kaldır",
                                    onClick = onNavigateToPremium
                                )
                            }
                        }
                    }

                    item(key = "general_settings_card") {
                        SettingsCard(title = "Genel Ayarlar") {
                            SettingsRowSwitch(
                                icon = Res.drawable.audio,
                                text = stringResource(Res.string.settings_sound),
                                checked = soundEnabled,
                                isNightMode = isNightModeEnabled,
                                onCheckedChange = { viewModel.setSoundEnabled(it) }
                            )
                            SettingsRowSwitch(
                                icon = Res.drawable.vibration,
                                text = stringResource(Res.string.settings_vibration),
                                checked = vibrationEnabled,
                                isNightMode = isNightModeEnabled,
                                onCheckedChange = { viewModel.setVibrationEnabled(it) }
                            )
                            SettingsRowSwitch(
                                icon = Res.drawable.darkmode,
                                text = stringResource(Res.string.settings_night_mode),
                                checked = isNightModeEnabled,
                                isNightMode = isNightModeEnabled,
                                onCheckedChange = { viewModel.setNightModeEnabled(it) }
                            )
                            SettingsRowSwitch(
                                icon = Res.drawable.fullscreen,
                                text = stringResource(Res.string.settings_fullscreen_touch),
                                checked = isFullScreenTouchEnabled,
                                isNightMode = isNightModeEnabled,
                                onCheckedChange = { viewModel.setFullScreenTouchEnabled(it) }
                            )
                        }
                    }

                    item(key = "accessibility_card") {
                        SettingsCard(title = "Erişilebilirlik") {
                            SettingsRowSwitch(
                                icon = Res.drawable.ic_read_aloud,
                                text = "Sayaç Okuma",
                                checked = isCounterReadingEnabled,
                                isNightMode = isNightModeEnabled,
                                onCheckedChange = { checked ->
                                    if (hasTtsEngines) {
                                        viewModel.setCounterReadingEnabled(checked)
                                    } else {
                                        showNoTtsEngineDialog = true
                                    }
                                },
                                enabled = hasTtsEngines
                            )

                            if (isCounterReadingEnabled && hasTtsEngines) {
                                Column(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp, start = 8.dp, end = 8.dp),
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                    verticalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    val selectedEngineLabel = ttsEngines.find { it.name == selectedTtsEngine }?.label
                                        ?: if (hasTtsEngines && selectedTtsEngine.isNotEmpty()) selectedTtsEngine else "Yükleniyor..."
                                    Text(
                                        text = "Tercih Edilen Motor",
                                        color = ZikrTheme.colors.primary,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    SettingsDialogDropdownButton(
                                        selectedLabel = selectedEngineLabel,
                                        onClick = { showTtsEngineDialog = true }
                                    )

                                    val currentLanguageLabel = availableLanguages.find { it.first == selectedTtsLanguage }?.second
                                        ?: if (isLanguageLoading) "Yükleniyor..." else "Dil Seçin"

                                    Text(
                                        text = "Tercih Edilen Dil",
                                        color = ZikrTheme.colors.primary,
                                        style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(bottom = 4.dp),
                                        textAlign = TextAlign.Center
                                    )
                                    SettingsDialogDropdownButton(
                                        selectedLabel = currentLanguageLabel,
                                        onClick = { showTtsLangDialog = true }
                                    )

                                    Column(
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Text(
                                            text = "Konuşma Hızı",
                                            color = ZikrTheme.colors.primary,
                                            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.Bold),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(bottom = 4.dp),
                                            textAlign = TextAlign.Center
                                        )
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Text("Yavaş", color = ZikrTheme.colors.secondary, style = MaterialTheme.typography.labelSmall)
                                            Text("Hızlı", color = ZikrTheme.colors.secondary, style = MaterialTheme.typography.labelSmall)
                                        }
                                        Slider(
                                            value = ttsSpeechRate,
                                            onValueChange = { viewModel.setTtsSpeechRate(it) },
                                            onValueChangeFinished = { viewModel.speakTestSound() },
                                            valueRange = 1.0f..2.5f,
                                            enabled = !isHuaweiEngine,
                                            steps = 5,
                                            modifier = Modifier.fillMaxWidth(),
                                            thumb = {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .background(
                                                            color = ZikrTheme.colors.primary,
                                                            shape = CircleShape
                                                        )
                                                )
                                            },
                                            track = { sliderState ->
                                                SliderDefaults.Track(
                                                    sliderState = sliderState,
                                                    modifier = Modifier.height(2.dp)
                                                )
                                            },
                                            colors = SliderDefaults.colors(
                                                activeTrackColor = ZikrTheme.colors.primary,
                                                inactiveTrackColor = Color.Gray,
                                                activeTickColor = ZikrTheme.colors.primary.copy(alpha = 0.5f),
                                                inactiveTickColor = Color.Transparent
                                            )
                                        )
                                        if (isHuaweiEngine) {
                                            Text(
                                                text = "Bu TTS motoru konuşma hızını desteklememektedir.",
                                                color = ZikrTheme.colors.secondary,
                                                style = MaterialTheme.typography.labelSmall,
                                                textAlign = TextAlign.Center,
                                                modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item(key = "app_settings_card") {
                        SettingsCard(title = "Uygulama") {
                            SettingsRowSwitch(
                                icon = Res.drawable.cloud_sync,
                                text = stringResource(Res.string.settings_backup),
                                checked = isBackupEnabled,
                                isNightMode = isNightModeEnabled,
                                onCheckedChange = { viewModel.onBackupSwitchToggled(it) }
                            )
                            SettingsRowClickable(
                                icon = Res.drawable.share,
                                text = stringResource(Res.string.settings_share),
                                onClick = { platformActionHandler.showShareSheet() }
                            )
                            SettingsRowClickable(
                                icon = Res.drawable.like,
                                text = stringResource(Res.string.settings_rate),
                                onClick = { platformActionHandler.openAppStore() }
                            )

                            val emailSubject = stringResource(Res.string.contact_email_subject)

                            SettingsRowClickable(
                                icon = Res.drawable.contact,
                                text = stringResource(Res.string.settings_contact_us),
                                onClick = {
                                    platformActionHandler.sendEmail(
                                        address = "hgtcsmsk@gmail.com",
                                        subject = emailSubject
                                    )
                                }
                            )
                            SettingsRowClickable(
                                icon = Res.drawable.privacy,
                                text = stringResource(Res.string.settings_privacy_policy),
                                onClick = {
                                    platformActionHandler.openUrl("https://hsynsmsk-art.github.io/privacy-policy-zikr-count")
                                }
                            )
                        }
                    }
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

            if (showTtsEngineDialog) {
                TtsSelectionDialog(
                    title = "Tercih Edilen Motor",
                    options = ttsEngines.map { it.label to it.name },
                    selectedValue = selectedTtsEngine,
                    onSelect = {
                        showTtsEngineDialog = false
                        viewModel.setSelectedTtsEngine(it)
                    },
                    onDismiss = { showTtsEngineDialog = false }
                )
            }
            if (showTtsLangDialog) {
                TtsSelectionDialog(
                    title = "Tercih Edilen Dil",
                    options = availableLanguages.map { it.second to it.first },
                    selectedValue = selectedTtsLanguage,
                    onSelect = {
                        showTtsLangDialog = false
                        viewModel.setSelectedTtsLanguage(it)
                    },
                    onDismiss = { showTtsLangDialog = false }
                )
            }
            if (showNoTtsEngineDialog) {
                ConfirmationDialog(
                    title = "Kullanılamıyor",
                    question = "Telefonunuzda bir TTS (Yazıdan Sese) motoru bulunmadığı için Sayaç Okuma özelliği kullanılamıyor.",
                    confirmButtonText = "Tamam",
                    onDismiss = { showNoTtsEngineDialog = false },
                    onConfirm = { showNoTtsEngineDialog = false }
                )
            }

            if (showUpdateDialog && updateState is UpdateState.Optional) {
                UpdateDialog(
                    updateState = updateState,
                    onConfirm = {
                        showUpdateDialog = false
                        platformActionHandler.openAppStore()
                    },
                    onDismiss = {
                        showUpdateDialog = false
                        viewModel.dismissUpdateDialog()
                    }
                )
            }
        }
    }
}


@Composable
private fun UpdateAvailableBanner(onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.Red.copy(alpha = 0.8f))
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Yeni sürüm mevcut",
            color = Color.White,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold
        )
    }
}


@Composable
private fun SettingsCard(
    title: String,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color.White.copy(alpha = 0.2f))
            .padding(16.dp)
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall.copy(fontWeight = FontWeight.Bold),
            color = Color.White,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun SettingsRowInfo(
    icon: DrawableResource,
    text: String,
    subText: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(ZikrTheme.colors.primary)
        )
        Column(
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        ) {
            Text(
                text = text,
                color = Color.White,
                style = MaterialTheme.typography.bodySmall
            )
            Text(
                text = subText,
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall
            )
        }
    }
}


@Composable
private fun SettingsRowSwitch(
    icon: DrawableResource,
    text: String,
    checked: Boolean,
    isNightMode: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    enabled: Boolean = true
) {
    val switchColors = SwitchDefaults.colors(
        checkedThumbColor = ZikrTheme.colors.primary,
        uncheckedThumbColor = Color.Gray,
        uncheckedTrackColor = Color.DarkGray,
        checkedTrackColor = if (isNightMode) {
            Color(0xFF212121) // Gece Modu: Çok Koyu Gri
        } else {
            Color.White       // Gündüz Modu: Beyaz
        }
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(ZikrTheme.colors.primary)
        )
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            enabled = enabled,
            modifier = Modifier.scale(0.9f),
            colors = switchColors
        )
    }
}

@Composable
private fun SettingsRowClickable(
    icon: DrawableResource,
    text: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(
            painter = painterResource(icon),
            contentDescription = text,
            modifier = Modifier.size(24.dp),
            colorFilter = ColorFilter.tint(ZikrTheme.colors.primary)
        )
        Text(
            text = text,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier
                .padding(start = 16.dp)
                .weight(1f)
        )
    }
}

@Composable
private fun SettingsDialogDropdownButton(
    selectedLabel: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(Color.Black.copy(alpha = 0.25f))
            .clickable { onClick() }
            .padding(12.dp)
    ) {
        Text(
            text = selectedLabel,
            color = Color.White,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.align(Alignment.CenterStart),
            textAlign = TextAlign.Start,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}