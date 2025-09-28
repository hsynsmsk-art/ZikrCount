package com.hgtcsmsk.zikrcount.ui.dialog

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import org.jetbrains.compose.resources.painterResource
import zikrcount.composeapp.generated.resources.Res
import zikrcount.composeapp.generated.resources.action_delete

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

    val title = if (isMandatory) "Güncelleme Gerekiyor" else "Yeni Sürüm Mevcut"
    val message = if (isMandatory) "Uygulamayı kullanmaya devam etmek için lütfen son sürüme güncelleyin." else "Uygulamanın yeni sürümü daha iyi bir deneyim sunuyor. Güncellemek ister misiniz?"

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
                    // --- DEĞİŞİKLİK EKLENDİ ---
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(text = "Yenilikler:", style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold), color = Color.Black)
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = notesText, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray)
            }

            // Butonlar Kısmı
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!isMandatory) {
                    OutlinedButton(
                        onClick = onDismiss,
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            "Sonra",
                            textAlign = TextAlign.Center,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }
                TextButton(
                    onClick = onConfirm,
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.textButtonColors(
                        containerColor = ZikrTheme.colors.primary,
                        contentColor = Color.Black
                    )
                ) {
                    Text(
                        "Güncelle",
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}