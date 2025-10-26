package com.hgtcsmsk.zikrcount.platform

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
expect fun BannerAd(
    modifier: Modifier,
    trigger: Int
)