package com.hgtcsmsk.zikrcount

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.hgtcsmsk.zikrcount.platform.NetworkMonitor

class MainActivity : ComponentActivity() {

    private val viewModel: AppViewModel by viewModels()

    // EKLENDİ: NetworkMonitor'ü başlat
    private val networkMonitor by lazy {
        NetworkMonitor(
            onNetworkAvailable = {
                runOnUiThread {
                    viewModel.onNetworkAvailable()
                }
            },
            onNetworkLost = {
                runOnUiThread {
                    viewModel.onNetworkLost()
                }
            }
        )
    }

    @OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }
        enableEdgeToEdge()
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            App(windowSizeClass = windowSizeClass, viewModel = viewModel)
        }
    }

    // EKLENDİ: onResume ve onPause fonksiyonları
    override fun onResume() {
        super.onResume()
        // Uygulama ön plana geldiğinde dinleyiciyi kaydet
        networkMonitor.register()
    }

    override fun onPause() {
        super.onPause()
        // Uygulama arka plana gittiğinde dinleyiciyi kaldır
        networkMonitor.unregister()
    }
}