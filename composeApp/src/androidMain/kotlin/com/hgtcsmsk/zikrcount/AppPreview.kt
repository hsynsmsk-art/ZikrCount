package com.hgtcsmsk.zikrcount

import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showSystemUi = true)
@Composable
fun AppPreview() {
    val previewWindowSizeClass = WindowSizeClass.calculateFromSize(DpSize(411.dp, 891.dp))
    App(windowSizeClass = previewWindowSizeClass, viewModel = AppViewModel())
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
@Preview(showSystemUi = true, device = "spec:width=1280dp,height=800dp,dpi=240")
@Composable
fun AppPreviewTablet() {
    val previewWindowSizeClass = WindowSizeClass.calculateFromSize(DpSize(1280.dp, 800.dp))
    App(windowSizeClass = previewWindowSizeClass, viewModel = AppViewModel())
}
