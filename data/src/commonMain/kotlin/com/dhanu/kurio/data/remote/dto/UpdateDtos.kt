package com.dhanu.kurio.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UpdateCheckResponse(
    val version: String,
    val title: String,
    val critical: Boolean,
    val description: String,
    val features: List<String> = emptyList(),
    @SerialName("release_date")
    val releaseDate: Long? = null,
    @SerialName("download_url")
    val downloadUrl: String? = null
)

@Serializable
data class ModelRegistryResponse(
    val models: List<ModelDefinitionDto> = emptyList(),
    val timestamp: Long = 0
)

@Serializable
data class ModelDefinitionDto(
    val id: String,
    val name: String,
    val description: String,
    val provider: String,
    val license: String,
    val language: String,
    @SerialName("size_bytes")
    val sizeBytes: Long,
    @SerialName("ram_usage_mb")
    val ramUsageMb: Int,
    @SerialName("speed_rating")
    val speedRating: Int,
    @SerialName("accuracy_rating")
    val accuracyRating: Int,
    @SerialName("download_url")
    val downloadUrl: String,
    val checksum: String,
    val version: String,
    @SerialName("device_class")
    val recommendedDeviceClass: String,
    @SerialName("is_experimental")
    val isExperimental: Boolean = false
)

@Serializable
data class AnnouncementResponse(
    val announcements: List<AnnouncementDto> = emptyList()
)

@Serializable
data class AnnouncementDto(
    val id: String,
    val title: String,
    val description: String,
    val type: String,
    @SerialName("action_text")
    val actionText: String? = null,
    @SerialName("action_url")
    val actionUrl: String? = null,
    val dismissible: Boolean = true,
    @SerialName("image_url")
    val imageUrl: String? = null
)
