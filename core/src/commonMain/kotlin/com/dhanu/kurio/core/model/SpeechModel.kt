package com.dhanu.kurio.core.model

import kotlinx.serialization.Serializable

@Serializable
data class SpeechModel(
    val id: String,
    val name: String,
    val description: String,
    val provider: String,
    val license: String,
    val language: String,
    val sizeBytes: Long,
    val ramUsageMb: Int,
    val speedRating: Int,
    val accuracyRating: Int,
    val downloadUrl: String,
    val checksum: String,
    val version: String,
    val recommendedDeviceClass: DeviceClass,
    val status: ModelStatus = ModelStatus.NOT_DOWNLOADED,
    val downloadProgress: Float = 0f,
    val isExperimental: Boolean = false
)

@Serializable
enum class ModelStatus {
    NOT_DOWNLOADED,
    DOWNLOADING,
    PAUSED,
    DOWNLOADED,
    VERIFYING,
    VERIFIED,
    CORRUPTED,
    ERROR,
    ACTIVE
}

@Serializable
enum class DeviceClass {
    LOW_END,
    MID_RANGE,
    HIGH_END,
    FLAGSHIP
}

@Serializable
data class ModelRegistryEntry(
    val model: SpeechModel,
    val downloadUrl: String,
    val checksum: String,
    val timestamp: Long
)

@Serializable
data class DownloadProgress(
    val modelId: String,
    val bytesDownloaded: Long,
    val totalBytes: Long,
    val speed: Long,
    val isPaused: Boolean = false,
    val isCancelled: Boolean = false
) {
    val progress: Float
        get() = if (totalBytes > 0) bytesDownloaded.toFloat() / totalBytes else 0f
}
