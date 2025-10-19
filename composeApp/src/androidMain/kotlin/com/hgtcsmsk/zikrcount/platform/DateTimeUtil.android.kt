package com.hgtcsmsk.zikrcount.platform

import java.text.DateFormat
import java.util.Date

actual fun formatTimestampToLocalDateTime(timestamp: Long, unknownDateText: String): String {
    return try {
        val date = Date(timestamp)
        val formatter = DateFormat.getDateTimeInstance(
            DateFormat.SHORT,
            DateFormat.SHORT
        )
        formatter.format(date)
    } catch (e: Exception) {
        e.printStackTrace()
        unknownDateText
    }
}