package com.hgtcsmsk.zikrcount.platform

actual fun getAppLanguageCode(): String = java.util.Locale.getDefault().language