package com.hgtcsmsk.zikrcount.data

import kotlinx.serialization.Serializable

/**
 * Bu enum sınıfı, güncelleme kontrolünün sonucunu temsil eder.
 * ViewModel bu durumu UI'a bildirmek için kullanacak.
 */
sealed class UpdateState {
    // Güncelleme yok veya kullanıcı en güncel sürümde.
    data object NoUpdate : UpdateState()

    // Nazikçe bildirilecek, isteğe bağlı bir güncelleme var.
    data class Optional(val info: UpdateInfo) : UpdateState()

    // Kullanıcının uygulamayı kullanmasını engelleyecek zorunlu bir güncelleme var.
    data class Mandatory(val info: UpdateInfo) : UpdateState()
}


/**
 * Bu veri sınıfı, internetten çekeceğimiz JSON dosyasının yapısını temsil eder.
 * @Serializable anotasyonu, kotlinx.serialization kütüphanesinin JSON'ı bu nesneye
 * otomatik olarak dönüştürmesini sağlar.
 */
@Serializable
data class UpdateInfo(
    val latestVersionCode: Int,
    val minimumRequiredVersionCode: Int,
    val updateNotes: Map<String, List<String>>
)