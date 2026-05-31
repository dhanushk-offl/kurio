package com.dhanu.kurio.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.dhanu.kurio.core.model.HistoryEntry

@Entity(tableName = "history")
data class HistoryEntity(
    @PrimaryKey
    val id: String,
    val text: String,
    val dateMillis: Long,
    val durationMs: Long,
    val wordCount: Int,
    val characterCount: Int,
    val modelId: String,
    val language: String
) {
    fun toDomain(): HistoryEntry = HistoryEntry(
        id = id,
        text = text,
        dateMillis = dateMillis,
        durationMs = durationMs,
        wordCount = wordCount,
        characterCount = characterCount,
        modelId = modelId,
        language = language
    )

    companion object {
        fun fromDomain(entry: HistoryEntry): HistoryEntity = HistoryEntity(
            id = entry.id,
            text = entry.text,
            dateMillis = entry.dateMillis,
            durationMs = entry.durationMs,
            wordCount = entry.wordCount,
            characterCount = entry.characterCount,
            modelId = entry.modelId,
            language = entry.language
        )
    }
}
