package com.hgtcsmsk.zikrcount.data

import io.ktor.client.HttpClient
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.statement.bodyAsText
import io.ktor.serialization.kotlinx.json.json
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json

class UpdateService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                useAlternativeNames = false
            })
        }
    }

    // --> DEĞİŞİKLİK: URL'yi daha temiz bir yapı için sabite taşıdık.
    private val updateJsonUrl = "https://gist.githubusercontent.com/hsynsmsk-art/20b6ab751871ada65f8fc64222ee6ba0/raw/version.json"

    suspend fun getUpdateInfo(): UpdateInfo? {
        return try {
            // --> DEĞİŞİKLİK 1: Önbelleği atlatmak için URL'nin sonuna anlık zaman damgası ekliyoruz.
            // Bu, her isteğin benzersiz olmasını sağlar ve ağ katmanlarının eski yanıtı dönmesini engeller.
            val timestamp = Clock.System.now().toEpochMilliseconds()
            val urlWithCacheBuster = "$updateJsonUrl?t=$timestamp"

            println("DEBUG: Güncelleme isteği yapılıyor: $urlWithCacheBuster")

            // --> DEĞİŞİKLİK 2: Ktor isteğini, başlık (header) ekleyebilmek ve yanıtı metin olarak alabilmek için güncelledik.
            // ContentNegotiation'ın otomatik parse etmesi yerine yanıtı manuel olarak kendimiz parse edeceğiz.
            val responseText = client.get(urlWithCacheBuster) {
                // Her ihtimale karşı "cache kullanma" başlıklarını da ekliyoruz.
                header("Cache-Control", "no-cache")
                header("Pragma", "no-cache")
            }.bodyAsText()

            println("DEBUG: Sunucudan gelen JSON: $responseText")

            // Yanıt metnini Kotlin nesnesine dönüştürüyoruz.
            Json { ignoreUnknownKeys = true }.decodeFromString<UpdateInfo>(responseText)
        } catch (e: Exception) {
            println("HATA: Güncelleme bilgisi alınamadı - ${e.message}")
            e.printStackTrace()
            null
        }
    }
}