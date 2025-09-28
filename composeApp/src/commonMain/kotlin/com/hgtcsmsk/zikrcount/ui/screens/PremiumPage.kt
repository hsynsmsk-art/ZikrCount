package com.hgtcsmsk.zikrcount.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.SystemBackButtonHandler
import com.hgtcsmsk.zikrcount.platform.rememberPlatformActivity
import com.hgtcsmsk.zikrcount.ui.components.ResponsiveText
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.utils.autoMirror
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import zikrcount.composeapp.generated.resources.*

@Composable
fun PremiumPage(
    viewModel: AppViewModel,
    onNavigateBack: () -> Unit,
) {
    SystemBackButtonHandler { onNavigateBack() }

    val purchaseState by viewModel.purchaseState.collectAsState()
    val productPrice by viewModel.productPrice.collectAsState()
    val selectedBackground by viewModel.selectedBackground.collectAsState()

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(findBackgroundResource(selectedBackground)),
            contentDescription = "Reklamları Kaldır Sayfası Arka Planı",
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
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(
                    painter = painterResource(Res.drawable.action_back),
                    contentDescription = "Geri",
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
                    text = "Reklamları Kaldır",
                    style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                    color = ZikrTheme.colors.primary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.weight(1f)
                )
                Spacer(modifier = Modifier.size(32.dp))
            }

            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .verticalScroll(rememberScrollState())
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                if (purchaseState is PurchaseState.Purchased) {
                    PurchasedUserContent()
                } else {
                    NotPurchasedUserContent(
                        purchaseState = purchaseState,
                        productPrice = productPrice,
                        onPurchaseClick = { activity -> viewModel.purchaseRemoveAds(activity) },
                        onRestoreClick = { viewModel.restorePurchases() }
                    )
                }
            }
        }
    }
}

@Composable
private fun NotPurchasedUserContent(
    purchaseState: PurchaseState,
    productPrice: String?,
    onPurchaseClick: (activity: Any) -> Unit,
    onRestoreClick: () -> Unit
) {
    val activity = rememberPlatformActivity()

    Image(
        painter = painterResource(Res.drawable.no_ads_new),
        contentDescription = "Reklam Yok İkonu",
        modifier = Modifier.size(100.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Reklamsız Deneyime Geçin",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Tek seferlik bir ödeme ile reklamları tamamen kaldırın ve tüm kilitli özellikleri açın.",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    val purchaseButtonText = when {
        purchaseState is PurchaseState.Loading -> "Yükleniyor..."
        productPrice != null -> "$productPrice ile Yükselt"
        else -> "Satın Al"
    }

    Button(
        onClick = { onPurchaseClick(activity) },
        modifier = Modifier.fillMaxWidth(0.9f).height(50.dp),
        colors = ButtonDefaults.buttonColors(containerColor = ZikrTheme.colors.primary),
        shape = RoundedCornerShape(12.dp),
        enabled = purchaseState !is PurchaseState.Pending && purchaseState !is PurchaseState.Loading
    ) {
        if (purchaseState is PurchaseState.Loading) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = Color.Black,
                strokeWidth = 2.dp
            )
        } else {
            ResponsiveText(
                text = purchaseButtonText,
                color = Color.Black,
                style = MaterialTheme.typography.bodyLarge.copy(fontWeight = FontWeight.Bold)
            )
        }
    }

    if (purchaseState is PurchaseState.Pending) {
        Text(
            text = "Ödemeniz onay bekliyor...",
            color = ZikrTheme.colors.primary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    TextButton(
        onClick = onRestoreClick,
        modifier = Modifier.padding(top = 8.dp)
    ) {
        Text("Satın Alımları Geri Yükle", color = Color.White.copy(alpha = 0.7f))
    }
}

@Composable
private fun PurchasedUserContent() {
    Image(
        painter = painterResource(Res.drawable.success_check),
        contentDescription = "Reklamlar Kaldırıldı",
        modifier = Modifier.size(100.dp),
        colorFilter = ColorFilter.tint(ZikrTheme.colors.primary)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = "Reklamlar Kaldırıldı",
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = "Uygulamanın reklamsız sürümünün ve tüm özelliklerinin keyfini çıkarıyorsunuz. Desteğiniz için teşekkür ederiz!",
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}

@Composable
private fun BenefitRow(icon: DrawableResource, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = ZikrTheme.colors.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(text = text, color = Color.White, style = MaterialTheme.typography.bodyMedium)
    }
}