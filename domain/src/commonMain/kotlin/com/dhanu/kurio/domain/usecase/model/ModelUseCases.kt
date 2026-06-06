package com.dhanu.kurio.domain.usecase.model

import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.core.model.EngineType
import com.dhanu.kurio.domain.repository.ModelRepository
import kotlinx.coroutines.flow.Flow

class GetModelsUseCase(
    private val repository: ModelRepository
) {
    operator fun invoke(): Flow<List<SpeechModel>> = repository.observeModels()
}

class GetModelUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(modelId: String): SpeechModel? = repository.getModel(modelId)
}

class GetModelsByEngineUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(engineType: EngineType): List<SpeechModel> =
        repository.getModelsByEngine(engineType)
}

class DownloadModelUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(modelId: String): Result<Unit> = repository.downloadModel(modelId)
}

class PauseDownloadUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(modelId: String) = repository.pauseDownload(modelId)
}

class ResumeDownloadUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(modelId: String) = repository.resumeDownload(modelId)
}

class DeleteModelUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(modelId: String) = repository.deleteModel(modelId)
}

class ActivateModelUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(modelId: String) = repository.activateModel(modelId)
}

class GetActiveModelUseCase(
    private val repository: ModelRepository
) {
    suspend operator fun invoke(): SpeechModel? = repository.getActiveModel()
}

class ObserveDownloadProgressUseCase(
    private val repository: ModelRepository
) {
    operator fun invoke(modelId: String): Flow<com.dhanu.kurio.core.model.DownloadProgress?> =
        repository.observeDownloadProgress(modelId)
}
