package com.hgtcsmsk.zikrcount.ui.dialog

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.hgtcsmsk.zikrcount.data.UpdateState
import com.hgtcsmsk.zikrcount.platform.getAppLanguageCode
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

@Composable
fun UpdateDialog(
    updateState: UpdateState,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    // Eğer güncelleme durumu NoUpdate ise diyaloğu hiç gösterme.
    if (updateState is UpdateState.NoUpdate) return

    val isMandatory = updateState is UpdateState.Mandatory
    val updateInfo = if (isMandatory) (updateState as UpdateState.Mandatory).info else (updateState as UpdateState.Optional).info

    // Cihazın diline göre güncelleme notlarını al, bulamazsa İngilizce'yi kullan.
    val notes = updateInfo.updateNotes[getAppLanguageCode()] ?: updateInfo.updateNotes["en"] ?: emptyList()
    val notesText = notes.joinToString(separator = "\n") { "• $it" }

    val title = if (isMandatory) {
        stringResource(Res.string.dialog_update_mandatory_title)
    } else {
        stringResource(Res.string.dialog_update_optional_title)
    }
    val message = if (isMandatory) {
        stringResource(Res.string.dialog_update_mandatory_message)
    } else {
        stringResource(Res.string.dialog_update_optional_message)
    }
    val whatsNewText = stringResource(Res.string.dialog_update_whats_new)
    val updateButtonText = stringResource(Res.string.action_update)

    Dialog(
        onDismissRequest = {
            // Güncelleme zorunlu ise, diyalog dışına basarak kapatılamaz.
            if (!isMandatory) {
                onDismiss()
            }
        },
        properties = DialogProperties(
            dismissOnBackPress = !isMandatory,
            dismissOnClickOutside = !isMandatory
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(ZikrTheme.colors.surface),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Başlık Kısmı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(ZikrTheme.colors.primary)
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = Color.Black,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center
                )
            }

            // İçerik Kısmı
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .verticalScroll(rememberScrollState())
            ) {
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = whatsNewText, style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = notesText, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }

            // Butonlar Kısmı (YENİ VE MERKEZİ YAPI)
            DialogButtons(
                onConfirm = onConfirm,
                onDismiss = onDismiss,
                confirmButtonText = updateButtonText,
                // Zorunlu güncelleme ise "İptal" ("Sonra") butonunu gizle
                showDismissButton = !isMandatory
            )
        }
    }
}