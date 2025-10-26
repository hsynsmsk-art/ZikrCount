package com.hgtcsmsk.zikrcount.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.data.Counter
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.Font
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import zikrcount.composeapp.generated.resources.*

@Composable
fun CounterDisplay(
    counter: Counter,
    countName: String,
    modifier: Modifier = Modifier,
    screenResource: DrawableResource,
    turModifier: Modifier = Modifier,
    isLandscape: Boolean = false,
    isTablet: Boolean = false
) {
    val isNamazHabilitates = counter.id == AppViewModel.NAMAZ_HABILITATES_COUNTER.id
    val isDefaultCounter = counter.id == AppViewModel.DEFAULT_COUNTER.id
    val isPhoneLandscape = isLandscape && !isTablet

    val displayCount = if (isNamazHabilitates) {
        counter.count % 33
    } else {
        counter.count
    }

    val aspectRatio = if (isLandscape) 2.9f else 1.55f
    val valueText = stringResource(Res.string.accessibility_value)
    val targetTextPrefix = stringResource(Res.string.accessibility_target)
    val dividerText = stringResource(Res.string.accessibility_divider)
    val roundTextPrefix = stringResource(Res.string.accessibility_round)
    val accessibilityText = buildString {
        append(countName)
        when {
            isDefaultCounter -> {
                append(", ")
                append(valueText)
                append(displayCount)
            }
            isNamazHabilitates -> {
                append(", ")
                append(targetTextPrefix)
                append(counter.count)
                append(dividerText)
                append(counter.target)
                append(", ")
                append(roundTextPrefix)
                append(counter.tur)
                append(", ")
                append(valueText)
                append(displayCount)
            }
            else -> {
                if (counter.target > 0) {
                    append(", ")
                    append(targetTextPrefix)
                    append(counter.target)
                }
                append(", ")
                append(roundTextPrefix)
                append(counter.tur)
                append(", ")
                append(valueText)
                append(displayCount)
            }
        }
    }

    Box(
        modifier = modifier
            .aspectRatio(aspectRatio)
            .background(
                color = ZikrTheme.colors.primary,
                shape = RoundedCornerShape(percent = if (isLandscape) 20 else 15)
            )
            .semantics {
                this.contentDescription = accessibilityText
            },
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(screenResource),
            contentDescription = null,
            modifier = Modifier
                .fillMaxSize(0.985f)
                .clearAndSetSemantics { }
        )

        val icyKutuOrania = when {
            isTablet && isLandscape -> 2.4f
            isPhoneLandscape -> 2f
            else -> 1.3f
        }

        Box(
            modifier = Modifier
                .fillMaxWidth(0.85f)
                .aspectRatio(icyKutuOrania),
            contentAlignment = Alignment.Center
        )  {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(vertical = 16.dp, horizontal = 4.dp)
            ) {
                Box(
                    modifier = Modifier.align(Alignment.TopCenter)
                ) {
                    val titleMediumFontSize = MaterialTheme.typography.titleMedium.fontSize.value
                    Text(
                        text = countName,
                        color = ZikrTheme.colors.secondary,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontSize = (titleMediumFontSize + if (isPhoneLandscape) 0 else 2).sp
                        ),
                        modifier = Modifier
                            .basicMarquee(iterations = Int.MAX_VALUE)
                            .clearAndSetSemantics { },
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
                Box(
                    modifier = Modifier.align(Alignment.Center)
                ) {
                    val baseStyle = MaterialTheme.typography.displayLarge
                    Text(
                        text = displayCount.toString(),
                        color = ZikrTheme.colors.textOnPrimary,
                        textAlign = TextAlign.Center,
                        style = baseStyle.copy(
                            fontFamily = FontFamily(Font(Res.font.digit_font)),
                            fontSize = baseStyle.fontSize * (if (isPhoneLandscape) 0.8f else 1.5f)
                        ),
                        modifier = Modifier.clearAndSetSemantics { },
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Box(
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val titleMediumFontSize = MaterialTheme.typography.titleMedium.fontSize.value
                        val targetText = when {
                            isNamazHabilitates -> stringResource(Res.string.home_display_target, counter.count) + "/" + counter.target
                            isDefaultCounter || counter.target <= 0 -> stringResource(Res.string.home_display_target, 0)
                            else -> stringResource(Res.string.home_display_target, counter.target)
                        }
                        Text(
                            text = targetText,
                            color = ZikrTheme.colors.secondary,
                            fontWeight = FontWeight.W400,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = (titleMediumFontSize + if (isPhoneLandscape) 0 else 2).sp
                            ),
                            modifier = Modifier.clearAndSetSemantics { }
                        )
                        Text(
                            modifier = turModifier
                                .clearAndSetSemantics { },
                            text = stringResource(Res.string.home_display_round, counter.tur),
                            color = ZikrTheme.colors.secondary,
                            fontWeight = FontWeight.W400,
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontSize = (titleMediumFontSize + if (isPhoneLandscape) 0 else 2).sp
                            ),
                        )
                    }
                }
            }
        }
    }
}