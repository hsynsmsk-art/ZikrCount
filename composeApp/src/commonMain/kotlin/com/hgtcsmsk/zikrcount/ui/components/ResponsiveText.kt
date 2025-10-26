package com.hgtcsmsk.zikrcount.ui.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign

@Composable
fun ResponsiveText(
    modifier: Modifier = Modifier,
    text: String,
    color: Color,
    textAlign: TextAlign = TextAlign.Center,
    style: TextStyle = MaterialTheme.typography.bodyMedium
) {
    var currentStyle by remember { mutableStateOf(style) }
    LaunchedEffect(text, style) {
        currentStyle = style
    }
    Text(
        text = text,
        color = color,
        textAlign = textAlign,
        style = currentStyle,
        maxLines = 1,
        softWrap = false,
        modifier = modifier,
        onTextLayout = { result ->
            if (result.didOverflowWidth) {
                currentStyle = currentStyle.copy(
                    fontSize = currentStyle.fontSize * 0.9f
                )
            }
        }
    )
}