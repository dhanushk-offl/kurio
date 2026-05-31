package com.dhanu.kurio.feature.history.viewmodel

import com.dhanu.kurio.core.model.HistoryEntry
import com.dhanu.kurio.domain.usecase.history.ObserveHistoryUseCase
import com.dhanu.kurio.domain.usecase.history.DeleteHistoryEntryUseCase
import com.dhanu.kurio.domain.usecase.history.ClearHistoryUseCase
import com.dhanu.kurio.domain.usecase.history.SearchHistoryUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(
    val entries: List<HistoryEntry> = emptyList(),
    val searchQuery: String = "",
    val isSearching: Boolean = false,
    val isLoading: Boolean = true
)

class HistoryViewModel(
    private val observeHistory: ObserveHistoryUseCase,
    private val deleteEntry: DeleteHistoryEntryUseCase,
    private val clearHistory: ClearHistoryUseCase,
    private val searchHistory: SearchHistoryUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    fun initialize() {
        scope.launch {
            observeHistory().collect { entries ->
                _uiState.update {
                    it.copy(
                        entries = entries,
                        isLoading = false
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query, isSearching = query.isNotBlank()) }
        if (query.isBlank()) return
        scope.launch {
            val results = searchHistory(query)
            _uiState.update { it.copy(entries = results) }
        }
    }

    fun onDeleteEntry(id: String) {
        scope.launch {
            deleteEntry(id)
        }
    }

    fun onClearHistory() {
        scope.launch {
            clearHistory()
        }
    }

    fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }
}
