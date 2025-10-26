package com.hgtcsmsk.zikrcount.ui.utils

actual fun String.format(vararg args: Any?): String {
    return java.lang.String.format(this, *args)
}