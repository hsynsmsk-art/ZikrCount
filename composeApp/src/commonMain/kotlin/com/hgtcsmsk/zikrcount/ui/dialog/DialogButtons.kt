package com.hgtcsmsk.zikrcount.ui.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.ui.components.ResponsiveText
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.Res
import zikrcount.composeapp.generated.resources.*

@Composable
fun DialogButtons(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String,
    isConfirmEnabled: Boolean = true
) {
    Row(
        modifier = Modifier
            .widthIn(max = 600.dp)
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        OutlinedButton(
            onClick = onDismiss,
            modifier = Modifier.weight(1f)
        ) {
            ResponsiveText(
                text = stringResource(Res.string.action_cancel),
                color = Color.Gray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
        TextButton(
            onClick = onConfirm,
            enabled = isConfirmEnabled,
            modifier = Modifier.weight(1f),
            colors = ButtonDefaults.textButtonColors(
                containerColor = ZikrTheme.colors.primary,
                contentColor = Color.Black,
                disabledContainerColor = Color.Gray,
                disabledContentColor = Color.DarkGray
            )
        ) {
            ResponsiveText(
                text = confirmButtonText,
                color = if (isConfirmEnabled) Color.Black else Color.DarkGray,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}