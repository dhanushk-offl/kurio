package com.dhanu.kurio.feature.home.viewmodel

import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.core.model.StorageUsage
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.core.util.StorageUtils
import com.dhanu.kurio.domain.usecase.history.GetHistoryCountUseCase
import com.dhanu.kurio.domain.usecase.model.GetActiveModelUseCase
import com.dhanu.kurio.domain.usecase.settings.CheckForUpdatesUseCase
import com.dhanu.kurio.domain.usecase.settings.GetStorageUsageUseCase
import com.dhanu.kurio.domain.usecase.transcription.ObserveTranscriptionStateUseCase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeUiState(
    val activeModel: SpeechModel? = null,
    val storageUsage: StorageUsage = StorageUsage(),
    val historyCount: Int = 0,
    val transcriptionState: TranscriptionState = TranscriptionState.IDLE,
    val isLoading: Boolean = true,
    val hasUpdate: Boolean = false,
    val updateVersion: String? = null
)

class HomeViewModel(
    private val getActiveModel: GetActiveModelUseCase,
    private val getStorageUsage: GetStorageUsageUseCase,
    private val getHistoryCount: GetHistoryCountUseCase,
    private val observeTranscriptionState: ObserveTranscriptionStateUseCase,
    private val checkForUpdates: CheckForUpdatesUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    fun initialize() {
        scope.launch {
            _uiState.update { it.copy(isLoading = true) }

            launch {
                observeTranscriptionState().collect { state ->
                    _uiState.update { it.copy(transcriptionState = state) }
                }
            }

            launch {
                _uiState.update {
                    it.copy(
                        activeModel = getActiveModel(),
                        historyCount = getHistoryCount(),
                        isLoading = false
                    )
                }
            }

            launch {
                getStorageUsage().collect { usage ->
                    _uiState.update { it.copy(storageUsage = usage) }
                }
            }

            launch {
                val updateResult = checkForUpdates()
                _uiState.update {
                    it.copy(
                        hasUpdate = updateResult.hasUpdate,
                        updateVersion = updateResult.latestVersion?.version
                    )
                }
            }
        }
    }

    fun refresh() {
        scope.launch {
            _uiState.update {
                it.copy(
                    activeModel = getActiveModel(),
                    historyCount = getHistoryCount()
                )
            }
        }
    }

    fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }
}
