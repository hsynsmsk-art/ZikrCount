package com.hgtcsmsk.zikrcount.data

import kotlinx.serialization.Serializable

sealed class UpdateState {

    data object NoUpdate : UpdateState()
    data class Optional(val info: UpdateInfo) : UpdateState()
    data class Mandatory(val info: UpdateInfo) : UpdateState()
}

@Serializable
data class UpdateInfo(
    val latestVersionCode: Int,
    val minimumRequiredVersionCode: Int,
    val updateNotes: Map<String, List<String>>
)