package com.hgtcsmsk.zikrcount.ui.dialog

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.ui.components.ResponsiveText
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*
import kotlin.math.max

@Composable
fun DialogButtons(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    confirmButtonText: String,
    isConfirmEnabled: Boolean = true,
    showDismissButton: Boolean = true
) {
    Layout(
        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
        content = {
            if (showDismissButton) {
                OutlinedButton(onClick = onDismiss) {
                    ResponsiveText(
                        text = stringResource(Res.string.action_cancel),
                        color = Color.Gray,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
            TextButton(
                onClick = onConfirm,
                enabled = isConfirmEnabled,
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
    ) { measurables, constraints ->

        val screenWidthDp = constraints.maxWidth / density
        val minButtonWidth = if (screenWidthDp > 500) 160.dp else 110.dp
        val minButtonWidthPx = minButtonWidth.roundToPx()
        val maxIntrinsicWidth = if (measurables.isNotEmpty()) {
            measurables.maxOf { it.maxIntrinsicWidth(constraints.maxHeight) }
        } else { 0 }

        val finalButtonWidth = max(maxIntrinsicWidth, minButtonWidthPx)
        val placeable = measurables.map { it.measure(constraints.copy(minWidth = finalButtonWidth)) }
        val buttonCount = placeable.size
        val totalWidthOfButtons = (finalButtonWidth * buttonCount).coerceAtMost(constraints.maxWidth)
        val totalSpacing = (constraints.maxWidth - totalWidthOfButtons)
        val gapCount = buttonCount + 1
        val gapSize = if (gapCount > 0) totalSpacing / gapCount else 0

        layout(constraints.maxWidth, placeable.maxOfOrNull { it.height } ?: 0) {
            var xPosition = gapSize
            placeable.forEach { placeable ->
                placeable.placeRelative(x = xPosition, y = 0)
                xPosition += finalButtonWidth + gapSize
            }
        }
    }
}