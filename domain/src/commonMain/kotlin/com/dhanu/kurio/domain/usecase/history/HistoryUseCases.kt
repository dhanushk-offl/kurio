package com.dhanu.kurio.domain.usecase.history

import com.dhanu.kurio.core.model.HistoryEntry
import com.dhanu.kurio.domain.repository.HistoryRepository
import kotlinx.coroutines.flow.Flow

class ObserveHistoryUseCase(
    private val repository: HistoryRepository
) {
    operator fun invoke(): Flow<List<HistoryEntry>> = repository.observeHistory()
}

class GetHistoryUseCase(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(): List<HistoryEntry> = repository.getHistory()
}

class AddHistoryEntryUseCase(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(entry: HistoryEntry) = repository.addEntry(entry)
}

class DeleteHistoryEntryUseCase(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(id: String) = repository.deleteEntry(id)
}

class ClearHistoryUseCase(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke() = repository.clearHistory()
}

class SearchHistoryUseCase(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(query: String): List<HistoryEntry> = repository.searchHistory(query)
}

class GetHistoryCountUseCase(
    private val repository: HistoryRepository
) {
    suspend operator fun invoke(): Int = repository.getHistoryCount()
}
