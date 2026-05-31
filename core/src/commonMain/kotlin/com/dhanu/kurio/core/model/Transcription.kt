package com.dhanu.kurio.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class TranscriptionState {
    IDLE,
    LISTENING,
    PROCESSING,
    COMPLETED,
    ERROR
}

@Serializable
data class TranscriptionResult(
    val id: String,
    val text: String,
    val date: Long,
    val durationMs: Long,
    val wordCount: Int,
    val characterCount: Int,
    val modelId: String,
    val language: String = "en"
)

@Serializable
data class TranscriptionStats(
    val wordCount: Int,
    val characterCount: Int,
    val durationMs: Long
)

@Serializable
data class TranscriptionError(
    val code: TranscriptionErrorCode,
    val message: String,
    val details: String? = null
)

@Serializable
enum class TranscriptionErrorCode {
    PERMISSION_DENIED,
    MICROPHONE_FAILURE,
    STORAGE_FULL,
    MODEL_MISSING,
    MODEL_CORRUPTED,
    DOWNLOAD_FAILED,
    NETWORK_FAILURE,
    UNKNOWN
}
