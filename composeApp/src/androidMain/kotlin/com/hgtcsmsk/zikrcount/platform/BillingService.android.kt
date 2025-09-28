package com.hgtcsmsk.zikrcount.platform

import android.app.Activity
import android.content.Context
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingFlowParams
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ProductDetails
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.android.billingclient.api.QueryProductDetailsParams
import com.android.billingclient.api.QueryPurchasesParams
import com.hgtcsmsk.zikrcount.data.appContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val PREMIUM_PRODUCT_ID = "remove_ads_premium" // Google Play Console'da oluşturacağınız ürünün kimliği

actual fun createBillingService(): BillingService {
    return ActualBillingService(appContext)
}

class ActualBillingService(context: Context) : BillingService, BillingClientStateListener {

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private val ioScope = CoroutineScope(Dispatchers.IO)

    private var productDetails: ProductDetails? = null

    private val _purchaseState = MutableStateFlow<PurchaseState>(PurchaseState.Loading)
    override val purchaseState = _purchaseState.asStateFlow()

    private val _productPrice = MutableStateFlow<String?>(null)
    override val productPrice = _productPrice.asStateFlow()

    private val purchasesUpdatedListener = PurchasesUpdatedListener { billingResult, purchases ->
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK && purchases != null) {
            for (purchase in purchases) {
                handlePurchase(purchase)
            }
        } else {
            // Hata durumlarını burada yönetebiliriz. Şimdilik sadece durumu geri alıyoruz.
            _purchaseState.value = PurchaseState.NotPurchased
        }
    }

    private val billingClient = BillingClient.newBuilder(context)
        .setListener(purchasesUpdatedListener)
        .enablePendingPurchases()
        .build()

    init {
        billingClient.startConnection(this)
    }

    override fun onBillingSetupFinished(billingResult: BillingResult) {
        if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
            // Billing servisine başarıyla bağlandık.
            // Şimdi ürün detaylarını ve eski satın alımları kontrol edelim.
            queryProductDetails()
            restorePurchases()
        }
    }

    override fun onBillingServiceDisconnected() {
        // Bağlantı koptu, tekrar bağlanmayı deneyebiliriz.
        billingClient.startConnection(this)
    }

    private fun queryProductDetails() {
        val productList = listOf(
            QueryProductDetailsParams.Product.newBuilder()
                .setProductId(PREMIUM_PRODUCT_ID)
                .setProductType(BillingClient.ProductType.INAPP)
                .build()
        )
        val params = QueryProductDetailsParams.newBuilder().setProductList(productList).build()

        billingClient.queryProductDetailsAsync(params) { _, productDetailsList ->
            if (productDetailsList.isNotEmpty()) {
                productDetails = productDetailsList[0]
                // Fiyatı alıp StateFlow'u güncelliyoruz.
                _productPrice.value = productDetails?.oneTimePurchaseOfferDetails?.formattedPrice
            }
        }
    }

    // --- DEĞİŞİKLİK: Fonksiyonun adı arayüze (interface) uyacak şekilde değiştirildi ---
    override fun purchaseRemoveAds(activity: Any) {
        if (!billingClient.isReady || productDetails == null) return

        val currentActivity = activity as? Activity ?: return

        val productDetailsParamsList = listOf(
            BillingFlowParams.ProductDetailsParams.newBuilder()
                .setProductDetails(productDetails!!)
                .build()
        )

        val billingFlowParams = BillingFlowParams.newBuilder()
            .setProductDetailsParamsList(productDetailsParamsList)
            .build()

        billingClient.launchBillingFlow(currentActivity, billingFlowParams)
    }

    override fun restorePurchases() {
        if (!billingClient.isReady) {
            _purchaseState.value = PurchaseState.NotPurchased
            return
        }

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                val premiumPurchase = purchases.find { it.products.contains(PREMIUM_PRODUCT_ID) }
                if (premiumPurchase != null) {
                    handlePurchase(premiumPurchase)
                } else {
                    _purchaseState.value = PurchaseState.NotPurchased
                }
            } else {
                _purchaseState.value = PurchaseState.NotPurchased
            }
        }
    }

    private fun handlePurchase(purchase: Purchase) {
        ioScope.launch {
            if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                if (!purchase.isAcknowledged) {
                    val acknowledgePurchaseParams = AcknowledgePurchaseParams.newBuilder()
                        .setPurchaseToken(purchase.purchaseToken)
                        .build()
                    withContext(Dispatchers.IO) {
                        billingClient.acknowledgePurchase(acknowledgePurchaseParams) { billingResult ->
                            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                                mainScope.launch { _purchaseState.value = PurchaseState.Purchased }
                            }
                        }
                    }
                } else {
                    mainScope.launch { _purchaseState.value = PurchaseState.Purchased }
                }
            } else if (purchase.purchaseState == Purchase.PurchaseState.PENDING) {
                mainScope.launch { _purchaseState.value = PurchaseState.Pending }
            }
        }
    }
}