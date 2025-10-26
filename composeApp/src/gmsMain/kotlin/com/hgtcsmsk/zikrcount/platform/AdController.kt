package com.hgtcsmsk.zikrcount.platform

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.FullScreenContentCallback
import com.google.android.gms.ads.LoadAdError
import com.google.android.gms.ads.rewarded.RewardedAd
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback
import com.hgtcsmsk.zikrcount.AppViewModel
import com.hgtcsmsk.zikrcount.data.appContext
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

private class AndroidAdController(
    private val activity: Activity?,
    private val viewModel: AppViewModel,
    private val onAdFailedToLoadSilent: (error: String) -> Unit
) : AdController {

    private var rewardedAd: RewardedAd? = null

    override var adState by mutableStateOf(RewardedAdState.NOT_LOADED)
        private set

    override suspend fun loadRewardAd(): Boolean {
        if (adState == RewardedAdState.LOADING || adState == RewardedAdState.LOADED) {
            return adState == RewardedAdState.LOADED
        }
        adState = RewardedAdState.LOADING

        return suspendCancellableCoroutine { continuation ->
            val adRequest = AdRequest.Builder().build()

            RewardedAd.load(
                appContext,
                "ca-app-pub-9986172682955464/9940564341",
                adRequest,
                object : RewardedAdLoadCallback() {
                    override fun onAdFailedToLoad(adError: LoadAdError) {
                        rewardedAd = null
                        adState = RewardedAdState.NOT_LOADED
                        onAdFailedToLoadSilent(adError.message)
                        if (continuation.isActive) {
                            continuation.resume(false)
                        }
                    }

                    override fun onAdLoaded(ad: RewardedAd) {
                        rewardedAd = ad
                        adState = RewardedAdState.LOADED
                        if (continuation.isActive) {
                            continuation.resume(true)
                        }
                    }
                }
            )
            continuation.invokeOnCancellation {
            }
        }
    }

    override fun showRewardAd(onAdResult: (ShowAdResult) -> Unit) {
        if (rewardedAd == null || adState != RewardedAdState.LOADED || activity == null) {
            onAdResult(ShowAdResult.Failed("Reklam hazır değil veya activity null."))
            return
        }

        rewardedAd?.fullScreenContentCallback = object : FullScreenContentCallback() {
            override fun onAdFailedToShowFullScreenContent(adError: com.google.android.gms.ads.AdError) {
                rewardedAd = null
                adState = RewardedAdState.NOT_LOADED
                viewModel.setAdPlayingState(false)
                onAdResult(ShowAdResult.Failed(adError.message))
            }

            override fun onAdShowedFullScreenContent() {
                viewModel.setAdPlayingState(true)
            }

            override fun onAdDismissedFullScreenContent() {
                rewardedAd = null
                adState = RewardedAdState.NOT_LOADED
                viewModel.setAdPlayingState(false)
            }
        }

        rewardedAd?.show(activity) {
            onAdResult(ShowAdResult.EarnedReward(true))
        }
    }
}

@Composable
actual fun rememberAdController(
    viewModel: AppViewModel,
    retryTrigger: Int,
    onAdFailedToLoad: (error: String) -> Unit
): AdController {
    val context = LocalContext.current
    val activity = remember(context) { context.findActivity() }

    return remember(activity, viewModel, onAdFailedToLoad, retryTrigger) {
        AndroidAdController(
            activity = activity,
            viewModel = viewModel,
            onAdFailedToLoadSilent = onAdFailedToLoad
        )
    }
}

private fun Context.findActivity(): Activity? {
    var context = this
    while (context is ContextWrapper) {
        if (context is Activity) return context
        context = context.baseContext
    }
    return null
}