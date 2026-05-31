package com.dhanu.kurio.data.repository

import com.dhanu.kurio.core.model.HistoryEntry
import com.dhanu.kurio.data.local.dao.HistoryDao
import com.dhanu.kurio.data.local.entity.HistoryEntity
import com.dhanu.kurio.domain.repository.HistoryRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class HistoryRepositoryImpl(
    private val historyDao: HistoryDao
) : HistoryRepository {

    override fun observeHistory(): Flow<List<HistoryEntry>> {
        return historyDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getHistory(): List<HistoryEntry> {
        return historyDao.getAll().map { it.toDomain() }
    }

    override suspend fun addEntry(entry: HistoryEntry) {
        historyDao.insert(HistoryEntity.fromDomain(entry))
        historyDao.trimToSize(HistoryEntry.MAX_HISTORY_SIZE)
    }

    override suspend fun deleteEntry(id: String) {
        historyDao.deleteById(id)
    }

    override suspend fun clearHistory() {
        historyDao.clearAll()
    }

    override suspend fun getEntry(id: String): HistoryEntry? {
        return historyDao.getById(id)?.toDomain()
    }

    override suspend fun searchHistory(query: String): List<HistoryEntry> {
        if (query.isBlank()) return getHistory()
        return historyDao.search(query).map { it.toDomain() }
    }

    override suspend fun getHistoryCount(): Int {
        return historyDao.getAll().size
    }
}
