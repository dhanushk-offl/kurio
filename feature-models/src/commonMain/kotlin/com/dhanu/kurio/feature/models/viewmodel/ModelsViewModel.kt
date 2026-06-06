package com.dhanu.kurio.feature.models.viewmodel

import com.dhanu.kurio.core.model.DownloadProgress
import com.dhanu.kurio.core.model.ModelStatus
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.domain.usecase.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ModelsUiState(
    val models: List<SpeechModel> = emptyList(),
    val downloadProgress: Map<String, Float> = emptyMap(),
    val downloadSpeeds: Map<String, String> = emptyMap(),
    val extractionProgress: Map<String, Float> = emptyMap(),
    val activeModelId: String? = null,
    val isLoading: Boolean = true,
    val error: String? = null
)

class ModelsViewModel(
    private val getModels: GetModelsUseCase,
    private val getModel: GetModelUseCase,
    private val downloadModel: DownloadModelUseCase,
    private val pauseDownload: PauseDownloadUseCase,
    private val resumeDownload: ResumeDownloadUseCase,
    private val deleteModel: DeleteModelUseCase,
    private val activateModel: ActivateModelUseCase,
    private val getActiveModel: GetActiveModelUseCase,
    private val observeDownloadProgress: ObserveDownloadProgressUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(ModelsUiState())
    val uiState: StateFlow<ModelsUiState> = _uiState.asStateFlow()

    fun initialize() {
        scope.launch {
            getModels().collect { models ->
                _uiState.update {
                    it.copy(
                        models = models,
                        isLoading = false
                    )
                }
            }
        }
        scope.launch {
            _uiState.update { it.copy(activeModelId = getActiveModel()?.id) }
        }
    }

    fun onDownloadModel(modelId: String) {
        scope.launch {
            _uiState.update { it.copy(error = null) }
            observeDownloadProgress(modelId).collect { progress ->
                if (progress != null) {
                    _uiState.update {
                        it.copy(
                            downloadProgress = it.downloadProgress + (modelId to progress.progress),
                            downloadSpeeds = it.downloadSpeeds + (modelId to formatSpeed(progress.speed))
                        )
                    }
                }
            }
        }
        scope.launch {
            val result = downloadModel(modelId)
            if (result.isFailure) {
                _uiState.update { it.copy(error = "Download failed: ${result.exceptionOrNull()?.message}") }
            }
        }
    }

    fun onPauseDownload(modelId: String) {
        scope.launch { pauseDownload(modelId) }
    }

    fun onResumeDownload(modelId: String) {
        onDownloadModel(modelId)
    }

    fun onDeleteModel(modelId: String) {
        scope.launch {
            deleteModel(modelId)
            _uiState.update {
                it.copy(
                    downloadProgress = it.downloadProgress - modelId,
                    downloadSpeeds = it.downloadSpeeds - modelId,
                    extractionProgress = it.extractionProgress - modelId
                )
            }
        }
    }

    fun onActivateModel(modelId: String) {
        scope.launch {
            activateModel(modelId)
            _uiState.update { it.copy(activeModelId = modelId) }
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }

    private fun formatSpeed(bytesPerSec: Long): String {
        return when {
            bytesPerSec >= 1_000_000 -> "${bytesPerSec / 1_000_000} MB/s"
            bytesPerSec >= 1_000 -> "${bytesPerSec / 1_000} KB/s"
            else -> "$bytesPerSec B/s"
        }
    }

    companion object {
        private operator fun <K, V> Map<K, V>.plus(pair: Pair<K, V>): Map<K, V> {
            val result = this.toMutableMap()
            result[pair.first] = pair.second
            return result
        }

        private operator fun <K, V> Map<K, V>.minus(key: K): Map<K, V> {
            val result = this.toMutableMap()
            result.remove(key)
            return result
        }
    }
}
