package com.hgtcsmsk.zikrcount.data

import kotlinx.serialization.Serializable
import kotlinx.datetime.Clock

@Serializable
data class Counter(
    val id: Long = Clock.System.now().toEpochMilliseconds(),
    val name: String,
    val count: Int = 0,
    val tur: Int = 0,
    val target: Int,
    val creationTimestamp: Long = id,
    val pinTimestamp: Long = 0L
)