package com.hgtcsmsk.zikrcount.platform

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.google.android.gms.ads.AdListener
import com.google.android.gms.ads.AdRequest
import com.google.android.gms.ads.AdSize
import com.google.android.gms.ads.AdView
import com.google.android.gms.ads.LoadAdError

@Composable
actual fun BannerAd(modifier: Modifier, trigger: Int) {
    val context = LocalContext.current
    val activity = context as? Activity ?: return
    val lifecycleOwner = LocalLifecycleOwner.current

    var isAdLoaded by remember { mutableStateOf(false) }

    val adView = remember {
        AdView(context)
    }

    DisposableEffect(lifecycleOwner, adView) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> adView.resume()
                Lifecycle.Event.ON_PAUSE -> adView.pause()
                Lifecycle.Event.ON_DESTROY -> adView.destroy()
                else -> {}
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            adView.destroy()
        }
    }

    // ### DÜZELTME: Sabit yükseklik kaldırıldı ###
    // Box'ın yüksekliğini, içindeki reklamın gerçek yüksekliğine göre
    // otomatik olarak ayarlaması için .wrapContentHeight() kullanıyoruz.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .wrapContentHeight() // .height(50.dp) yerine bu kullanıldı.
            .background(Color.Transparent)
    ) {
        // AndroidView'ın update bloğu, trigger gibi bir anahtar değiştiğinde yeniden çalışır.
        AndroidView(
            factory = { adView },
            update = { view ->
                val displayMetrics = view.context.resources.displayMetrics
                val adWidth = (displayMetrics.widthPixels / view.context.resources.displayMetrics.density).toInt()

                view.setAdSize(
                    AdSize.getCurrentOrientationAnchoredAdaptiveBannerAdSize(context, adWidth)
                )
                view.adUnitId = "ca-app-pub-9986172682955464/9149678118"

                view.setAdListener(object : AdListener() {
                    override fun onAdLoaded() {
                        isAdLoaded = true
                    }

                    override fun onAdFailedToLoad(error: LoadAdError) {
                        isAdLoaded = false
                        println("Ad failed to load: ${error.message}")
                    }
                })

                view.loadAd(AdRequest.Builder().build())
            },
            // Reklam yüklenmediyse, bu bileşenin hiç yer kaplamamasını sağlıyoruz.
            modifier = if (isAdLoaded) Modifier.fillMaxWidth() else Modifier.height(0.dp)
        )
    }
}
