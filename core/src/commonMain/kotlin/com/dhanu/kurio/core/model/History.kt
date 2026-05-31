package com.dhanu.kurio.core.model

import kotlinx.serialization.Serializable

@Serializable
data class HistoryEntry(
    val id: String,
    val text: String,
    val dateMillis: Long,
    val durationMs: Long,
    val wordCount: Int,
    val characterCount: Int,
    val modelId: String,
    val language: String = "en"
) {
    companion object {
        const val MAX_HISTORY_SIZE = 10
    }
}
