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
        // NİHAİ ÇÖZÜM: Hem başlık çubuğunu kaldıran hem de kenardan kenara
        // görünümü sağlayan iki yöntem birleştirildi.
        setTheme(R.style.Theme_App)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val splashScreen = installSplashScreen()
        splashScreen.setKeepOnScreenCondition {
            viewModel.isLoading.value
        }

        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN)

        setContent {
            val windowSizeClass = calculateWindowSizeClass(this)
            App(windowSizeClass = windowSizeClass, viewModel = viewModel)
        }
    }

    override fun onResume() {
        super.onResume()
        networkMonitor.register()
        // <-- DEĞİŞİKLİK BAŞLIYOR -->
        viewModel.checkAccessibilityStatusOnResume()
        // <-- DEĞİŞİKLİK BİTİYOR -->
    }

    override fun onPause() {
        super.onPause()
        networkMonitor.unregister()
    }
}