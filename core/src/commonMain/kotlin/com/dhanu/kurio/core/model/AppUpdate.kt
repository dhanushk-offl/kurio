package com.dhanu.kurio.core.model

import kotlinx.serialization.Serializable

@Serializable
data class AppVersion(
    val version: String,
    val title: String,
    val critical: Boolean,
    val description: String,
    val features: List<String> = emptyList(),
    val releaseDate: Long? = null,
    val downloadUrl: String? = null
)

@Serializable
data class FeatureAnnouncement(
    val id: String,
    val title: String,
    val description: String,
    val type: AnnouncementType,
    val actionText: String? = null,
    val actionUrl: String? = null,
    val dismissible: Boolean = true,
    val imageUrl: String? = null
)

@Serializable
enum class AnnouncementType {
    NEW_VERSION,
    NEW_MODEL,
    FEATURE_RELEASE,
    TIP
}

@Serializable
data class UpdateCheckResult(
    val hasUpdate: Boolean,
    val latestVersion: AppVersion? = null,
    val announcements: List<FeatureAnnouncement> = emptyList(),
    val checkTimestamp: Long
)

@Serializable
enum class ReleaseChannel {
    STABLE,
    BETA,
    DEVELOPER_PREVIEW
}

@Serializable
data class ChangelogEntry(
    val version: String,
    val title: String,
    val releaseDate: Long,
    val features: List<String> = emptyList(),
    val bugFixes: List<String> = emptyList(),
    val modelReleases: List<String> = emptyList(),
    val isCritical: Boolean = false
)
