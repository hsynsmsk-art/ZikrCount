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
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.platform.PurchaseState
import com.hgtcsmsk.zikrcount.platform.SystemBackButtonHandler
import com.hgtcsmsk.zikrcount.platform.rememberPlatformActivity
import com.hgtcsmsk.zikrcount.ui.components.ResponsiveText
import com.hgtcsmsk.zikrcount.ui.components.SuccessSnackBar
import com.hgtcsmsk.zikrcount.ui.theme.ZikrTheme
import com.hgtcsmsk.zikrcount.ui.utils.autoMirror
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.DrawableResource
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
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
    val isRestoring by viewModel.isRestoringPurchases.collectAsState()

    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()

    // HATA DÜZELTMESİ: Snackbar mesajlarını string.xml'den al
    val noPurchaseMessage = stringResource(Res.string.snackbar_restore_no_purchase)
    val errorMessage = stringResource(Res.string.snackbar_restore_error)

    LaunchedEffect(key1 = Unit) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is AppViewModel.UiEvent.ShowSnackbar -> {
                    scope.launch {
                        snackbarHostState.showSnackbar(event.message)
                    }
                }
            }
        }
    }

    Scaffold(
        contentWindowInsets = WindowInsets.safeDrawing,
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                SuccessSnackBar(data = data)
            }
        },
        containerColor = Color.Transparent
    ) { innerPadding ->
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            Image(
                painter = painterResource(findBackgroundResource(selectedBackground)),
                // DEĞİŞİKLİK 1: Dekoratif arka plan resmi TalkBack tarafından okunmamalı.
                contentDescription = null,
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
                        text = stringResource(Res.string.premium_page_title),
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = ZikrTheme.colors.primary,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.size(32.dp))
                }

                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
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
                            isRestoring = isRestoring,
                            onPurchaseClick = { activity -> viewModel.purchaseRemoveAds(activity) },
                            // HATA DÜZELTMESİ: Eksik parametreleri viewModel'e yolla
                            onRestoreClick = { viewModel.restorePurchases(noPurchaseMessage, errorMessage) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun NotPurchasedUserContent(
    purchaseState: PurchaseState,
    productPrice: String?,
    isRestoring: Boolean,
    onPurchaseClick: (activity: Any) -> Unit,
    onRestoreClick: () -> Unit
) {
    val activity = rememberPlatformActivity()

    Image(
        painter = painterResource(Res.drawable.no_ads_new),
        // DEĞİŞİKLİK 2: "Reklam Yok" ikonu dekoratif olduğu için okunmamalı. Anlamı aşağıdaki başlıkta zaten var.
        contentDescription = null,
        modifier = Modifier.size(100.dp)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(Res.string.premium_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(Res.string.premium_description),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(32.dp))

    val purchaseButtonText = when {
        purchaseState is PurchaseState.Loading -> stringResource(Res.string.premium_purchase_button_loading)
        productPrice != null -> stringResource(Res.string.premium_purchase_button_price, productPrice)
        else -> stringResource(Res.string.premium_purchase_button_default)
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
            text = stringResource(Res.string.premium_purchase_pending),
            color = ZikrTheme.colors.primary,
            style = MaterialTheme.typography.bodySmall,
            modifier = Modifier.padding(top = 8.dp)
        )
    }

    // DEĞİŞİKLİK 3: Yükleme durumunu TalkBack'e bildirmek için Box'a semantics eklendi.
    val restoreStatusText = if (isRestoring) stringResource(Res.string.premium_restore_loading) else ""
    Box(
        modifier = Modifier
            .padding(top = 8.dp)
            .semantics(mergeDescendants = true) {
                // Eğer yükleniyorsa, kutunun açıklaması bu metin olacak.
                if (isRestoring) contentDescription = restoreStatusText
            },
        contentAlignment = Alignment.Center
    ) {
        TextButton(
            onClick = onRestoreClick,
            enabled = !isRestoring
        ) {
            Text(stringResource(Res.string.premium_restore_button), color = Color.White.copy(alpha = if (isRestoring) 0.3f else 0.7f))
        }
        if (isRestoring) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
                color = Color.White
            )
        }
    }
}

@Composable
private fun PurchasedUserContent() {
    Image(
        painter = painterResource(Res.drawable.success_check),
        // DEĞİŞİKLİK 2: "Onay" ikonu dekoratif olduğu için okunmamalı. Anlamı aşağıdaki başlıkta zaten var.
        contentDescription = null,
        modifier = Modifier.size(100.dp),
        colorFilter = ColorFilter.tint(ZikrTheme.colors.primary)
    )

    Spacer(modifier = Modifier.height(24.dp))

    Text(
        text = stringResource(Res.string.premium_already_purchased_title),
        style = MaterialTheme.typography.titleLarge,
        fontWeight = FontWeight.Bold,
        color = Color.White,
        textAlign = TextAlign.Center
    )

    Spacer(modifier = Modifier.height(16.dp))

    Text(
        text = stringResource(Res.string.premium_already_purchased_description),
        style = MaterialTheme.typography.bodyMedium,
        color = Color.White.copy(alpha = 0.8f),
        textAlign = TextAlign.Center
    )
}