package com.hgtcsmsk.zikrcount.platform

import kotlinx.coroutines.flow.StateFlow


sealed class RestoreResult {
    data object Success : RestoreResult()
    data object NoPurchasesFound : RestoreResult()
    data object Error : RestoreResult()
}

sealed class PurchaseState {
    data object NotPurchased : PurchaseState()
    data object Purchased : PurchaseState()
    data object Pending : PurchaseState()
    data object Loading : PurchaseState()
}


interface BillingService {
    val purchaseState: StateFlow<PurchaseState>
    val productPrice: StateFlow<String?>

    fun purchaseRemoveAds(activity: Any)

    suspend fun restorePurchases(): RestoreResult
}

expect fun createBillingService(): BillingService