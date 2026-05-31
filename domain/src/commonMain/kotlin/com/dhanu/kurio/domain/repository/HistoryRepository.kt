package com.dhanu.kurio.domain.repository

import com.dhanu.kurio.core.model.HistoryEntry
import kotlinx.coroutines.flow.Flow

interface HistoryRepository {
    fun observeHistory(): Flow<List<HistoryEntry>>
    suspend fun getHistory(): List<HistoryEntry>
    suspend fun addEntry(entry: HistoryEntry)
    suspend fun deleteEntry(id: String)
    suspend fun clearHistory()
    suspend fun getEntry(id: String): HistoryEntry?
    suspend fun searchHistory(query: String): List<HistoryEntry>
    suspend fun getHistoryCount(): Int
}
