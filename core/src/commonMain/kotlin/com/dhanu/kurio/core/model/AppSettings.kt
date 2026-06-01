package com.dhanu.kurio.core.model

import kotlin.math.pow
import kotlin.math.round
import kotlinx.serialization.Serializable

@Serializable
data class StorageUsage(
    val modelsBytes: Long = 0,
    val historyBytes: Long = 0,
    val cacheBytes: Long = 0
) {
    val totalBytes: Long get() = modelsBytes + historyBytes + cacheBytes

    val formattedModels: String get() = formatBytes(modelsBytes)
    val formattedHistory: String get() = formatBytes(historyBytes)
    val formattedCache: String get() = formatBytes(cacheBytes)
    val formattedTotal: String get() = formatBytes(totalBytes)

    companion object {
        private fun roundTo(value: Double, decimals: Int): String {
            val factor = 10.0.pow(decimals)
            val rounded = round(value * factor) / factor
            val parts = rounded.toString().split(".")
            val intPart = parts[0]
            val decPart = if (parts.size > 1) parts[1] else ""
            return "$intPart.${decPart.padEnd(decimals, '0').take(decimals)}"
        }

        fun formatBytes(bytes: Long): String {
            return when {
                bytes < 1024 -> "$bytes B"
                bytes < 1024 * 1024 -> "${bytes / 1024} KB"
                bytes < 1024 * 1024 * 1024 -> "${roundTo(bytes.toDouble() / (1024 * 1024), 1)} MB"
                else -> "${roundTo(bytes.toDouble() / (1024 * 1024 * 1024), 2)} GB"
            }
        }
    }
}

@Serializable
data class AppPreferences(
    val autoCopyEnabled: Boolean = true,
    val hapticFeedbackEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val analyticsEnabled: Boolean = false,
    val notificationsEnabled: Boolean = true,
    val releaseChannel: ReleaseChannel = ReleaseChannel.STABLE,
    val selectedModelId: String = "whisper-tiny-en",
    val lastUpdateCheck: Long = 0L
)
