package com.hgtcsmsk.zikrcount.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
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
    screenResource: DrawableResource = Res.drawable.screen,
    turModifier: Modifier = Modifier,
) {
    val isNamazTesbihati = counter.id == AppViewModel.NAMAZ_TESBIHATI_COUNTER.id
    val isDefaultCounter = counter.id == AppViewModel.DEFAULT_COUNTER.id

    val displayCount = if (isNamazTesbihati) {
        counter.count % 33
    } else {
        counter.count
    }

    Box(
        modifier = modifier
            .fillMaxSize(0.82f)
            .aspectRatio(1.55f)
            .background(
                color = ZikrTheme.colors.primary,
                shape = RoundedCornerShape(percent = 15)
            ),
        contentAlignment = Alignment.Center
    ){
        Image(
            painter = painterResource(screenResource),
            contentDescription = stringResource(Res.string.content_desc_main_display),
            modifier = Modifier.fillMaxSize(0.97f)
        )
        BoxWithConstraints(
            modifier = Modifier.fillMaxWidth(0.83f).aspectRatio(1.42f),
            contentAlignment = Alignment.Center
        ) {
            val density = LocalDensity.current
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Box(
                    modifier = Modifier.weight(15f),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSize = with(density) {
                        (this@BoxWithConstraints.maxHeight * 0.105f).toSp()
                    }
                    Text(
                        text = countName,
                        color = ZikrTheme.colors.secondary,
                        fontWeight = FontWeight.W400,
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        modifier = Modifier.basicMarquee(iterations = Int.MAX_VALUE),
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Clip
                    )
                }
                Box(
                    modifier = Modifier.weight(50f),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSize = with(density) {
                        (this@BoxWithConstraints.maxHeight * 0.6f).toSp()
                    }
                    Text(
                        text = displayCount.toString(),
                        color = ZikrTheme.colors.textOnPrimary,
                        fontFamily = FontFamily(Font(Res.font.digit_font)),
                        textAlign = TextAlign.Center,
                        fontSize = fontSize,
                        lineHeight = fontSize,
                        maxLines = 1,
                        softWrap = false
                    )
                }
                Box(
                    modifier = Modifier.weight(15f),
                    contentAlignment = Alignment.Center
                ) {
                    val fontSize = with(density) {
                        (this@BoxWithConstraints.maxHeight * 0.105f).toSp()
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        val targetText = when {
                            isNamazTesbihati -> {
                                // Namaz tesbihatı için özel formatı doğrudan burada oluşturalım
                                "Hedef: ${counter.count}/${counter.target}"
                            }
                            isDefaultCounter || counter.target <= 0 -> {
                                // Varsayılan sayaç VEYA hedef 0 ise, mevcut string kaynağını kullanarak "Hedef: 0" yazdıralım
                                stringResource(Res.string.home_display_target, 0)
                            }
                            else -> {
                                // Diğer tüm durumlarda gerçek hedefi yazdıralım
                                stringResource(Res.string.home_display_target, counter.target)
                            }
                        }
                        Text(
                            text = targetText,
                            color = ZikrTheme.colors.secondary,
                            fontWeight = FontWeight.W400,
                            fontSize = fontSize,
                            lineHeight = fontSize
                        )
                        Text(
                            modifier = turModifier,
                            text = stringResource(Res.string.home_display_round, counter.tur),
                            color = ZikrTheme.colors.secondary,
                            fontWeight = FontWeight.W400,
                            fontSize = fontSize,
                            lineHeight = fontSize
                        )
                    }
                }
            }
        }
    }
}