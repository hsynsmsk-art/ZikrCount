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

    private val updateJsonUrl = "https://gist.githubusercontent.com/hsynsmsk-art/20b6ab751871ada65f8fc64222ee6ba0/raw/version.json"

    suspend fun getUpdateInfo(): UpdateInfo? {
        return try {

            val timestamp = Clock.System.now().toEpochMilliseconds()
            val urlWithCacheBuster = "$updateJsonUrl?t=$timestamp"

            println("DEBUG: Making update request: $urlWithCacheBuster")

            val responseText = client.get(urlWithCacheBuster) {
                header("Cache-Control", "no-cache")
                header("Pragma", "no-cache")
            }.bodyAsText()

            println("DEBUG: JSON response from server: $responseText")

            Json { ignoreUnknownKeys = true }.decodeFromString<UpdateInfo>(responseText)

        } catch (e: Exception) {
            println("ERROR: Failed to retrieve update info - ${e.message}")
            e.printStackTrace()
            null
        }
    }
}