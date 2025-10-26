package com.hgtcsmsk.zikrcount.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.ui.utils.pressable
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource

@Composable
fun TopActionButton(
    iconResource: DrawableResource,
    contentDescription: String,
    onClick: () -> Unit
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .padding(6.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick() }
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(4.dp)
        ) {
            val strokeColor = Color.Black.copy(alpha = 0.7f)
            val strokeWidth = 1.dp
            Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(x = strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
            Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(x = -strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
            Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(y = strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
            Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(y = -strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
            Image(painter = painterResource(iconResource), contentDescription = contentDescription, colorFilter = ColorFilter.tint(Color.White))
        }
    }
}

@Composable
fun SmallActionButton(
    iconResource: DrawableResource,
    contentDescription: String,
    modifier: Modifier = Modifier,
    applyManualStroke: Boolean = false,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .aspectRatio(1f)
            .clip(CircleShape)
            .background(color = Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            }
            .pressable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier.fillMaxSize(0.7f),
            contentAlignment = Alignment.Center
        ) {
            if (applyManualStroke) {
                val strokeColor = Color.Black.copy(alpha = 0.7f)
                val strokeWidth = 1.dp
                Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(x = strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
                Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(x = -strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
                Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(y = strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
                Image(painter = painterResource(iconResource), contentDescription = null, modifier = Modifier.offset(y = -strokeWidth), colorFilter = ColorFilter.tint(strokeColor))
                Image(painter = painterResource(iconResource), contentDescription = null, colorFilter = ColorFilter.tint(Color.White))
            } else {
                Image(
                    painter = painterResource(iconResource),
                    contentDescription = null
                )
            }
        }
    }
}

@Composable
fun TabletActionButton(
    iconResource: DrawableResource,
    contentDescription: String,
    modifier: Modifier = Modifier,
    tintIcon: Boolean = true,
    onClick: () -> Unit = {}
) {
    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color = Color.White.copy(alpha = 0.2f))
            .border(1.dp, Color.White.copy(alpha = 0.3f), CircleShape)
            .clickable { onClick() }
            .semantics {
                this.contentDescription = contentDescription
                this.role = Role.Button
            },
        contentAlignment = Alignment.Center
    ) {
        val colorFilter = if (tintIcon) ColorFilter.tint(Color.White) else null
        Image(
            painter = painterResource(iconResource),
            contentDescription = null,
            colorFilter = colorFilter,
            modifier = Modifier.fillMaxSize(0.7f)
        )
    }
}