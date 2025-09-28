package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable
import com.hgtcsmsk.zikrcount.AppViewModel

enum class RewardedAdState {
    NOT_LOADED,
    LOADING,
    LOADED
}

sealed class ShowAdResult {
    data object Shown : ShowAdResult()
    data class Failed(val error: String) : ShowAdResult()
    data class EarnedReward(val earned: Boolean) : ShowAdResult()
}

interface AdController {
    val adState: RewardedAdState
    suspend fun loadRewardAd(): Boolean
    fun showRewardAd(onAdResult: (ShowAdResult) -> Unit)
}

@Composable
expect fun rememberAdController(
    viewModel: AppViewModel,
    retryTrigger: Int,
    onAdFailedToLoad: (error: String) -> Unit
): AdController