package com.dhanu.kurio.data.remote.api

import com.dhanu.kurio.data.remote.dto.UpdateCheckResponse
import com.dhanu.kurio.data.remote.dto.ModelRegistryResponse
import com.dhanu.kurio.data.remote.dto.AnnouncementResponse
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.utils.io.*

open class KurioApi(
    private val client: HttpClient
) {
    private companion object {
        const val BASE_URL = "https://api.kurio.app/v1"
        const val UPDATE_ENDPOINT = "$BASE_URL/update"
        const val REGISTRY_ENDPOINT = "$BASE_URL/models"
        const val ANNOUNCEMENTS_ENDPOINT = "$BASE_URL/announcements"
        const val CHANGELOG_ENDPOINT = "$BASE_URL/changelog"
    }

    open suspend fun checkForUpdate(
        currentVersion: String,
        channel: String = "stable"
    ): UpdateCheckResponse {
        return client.get(UPDATE_ENDPOINT) {
            parameter("current_version", currentVersion)
            parameter("channel", channel)
        }.body()
    }

    suspend fun getModelRegistry(): ModelRegistryResponse {
        return client.get(REGISTRY_ENDPOINT).body()
    }

    suspend fun getAnnouncements(
        dismissedIds: List<String> = emptyList()
    ): AnnouncementResponse {
        return client.get(ANNOUNCEMENTS_ENDPOINT) {
            parameter("dismissed", dismissedIds.joinToString(","))
        }.body()
    }

    suspend fun getChangelog(): List<UpdateCheckResponse> {
        return client.get(CHANGELOG_ENDPOINT).body()
    }

    suspend fun downloadModel(url: String): ByteArray {
        return client.get(url).body()
    }

    data class StreamResult(
        val channel: ByteReadChannel,
        val contentLength: Long?,
        val statusCode: Int
    )

    suspend fun downloadModelStreaming(
        url: String,
        rangeStart: Long? = null
    ): StreamResult {
        val response = client.get(url) {
            if (rangeStart != null && rangeStart > 0) {
                header(HttpHeaders.Range, "bytes=$rangeStart-")
            }
        }
        return StreamResult(
            channel = response.bodyAsChannel(),
            contentLength = response.contentLength(),
            statusCode = response.status.value
        )
    }
}
