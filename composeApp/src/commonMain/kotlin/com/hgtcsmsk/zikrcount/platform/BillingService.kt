package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable
import kotlinx.coroutines.flow.StateFlow

/**
 * Satın alma durumunu temsil eden mühürlü bir sınıf.
 * Bu sayede bir ürünün durumunu (alınmadı, beklemede, alındı) net bir şekilde takip edebiliriz.
 */
sealed class PurchaseState {
    // Kullanıcı ürünü henüz satın almadı.
    data object NotPurchased : PurchaseState()

    // Kullanıcı ürünü satın aldı ve bu durum geçerli.
    data object Purchased : PurchaseState()

    // Ödeme işlemi devam ediyor, henüz tamamlanmadı (örneğin yavaş kart işlemleri).
    data object Pending : PurchaseState()

    // Fiyat veya ürün bilgisi yükleniyor.
    data object Loading : PurchaseState()
}

/**
 * Bu, tüm platformların (Android, iOS) uygulaması gereken Satın Alma Servisi'nin kontratıdır.
 * Ne gibi yetenekleri olması gerektiğini burada tanımlarız.
 */
interface BillingService {
    /**
     * Kullanıcının ürünü alıp almadığını gösteren anlık durum.
     * UI bu durumu dinleyerek kendini günceller.
     */
    val purchaseState: StateFlow<PurchaseState>

    /**
     * Google Play'den veya App Store'dan ürünün yerelleştirilmiş fiyatını çeker (örn: "49,99 TL").
     * Bu da anlık bir durumdur, yüklendiğinde güncellenir.
     */
    val productPrice: StateFlow<String?>

    /**
     * "Reklamları Kaldır" ürününü satın alma akışını başlatır.
     * Activity parametresi, Android gibi platformların UI'ı başlatması için gereklidir.
     */
    fun purchaseRemoveAds(activity: Any)

    /**
     * Kullanıcının daha önceden yaptığı satın alımları kontrol edip geri yükler.
     */
    fun restorePurchases()
}

/**
 * ViewModel gibi Composable olmayan yerlerden BillingService'e erişmek için
 * her platformun bir yaratıcı fonksiyon sunmasını bekleriz.
 */
expect fun createBillingService(): BillingService