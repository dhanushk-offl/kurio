package com.dhanu.kurio.domain.repository

import com.dhanu.kurio.core.model.DownloadProgress
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.core.model.ModelStatus
import com.dhanu.kurio.core.model.EngineType
import kotlinx.coroutines.flow.Flow

interface ModelRepository {
    fun observeModels(): Flow<List<SpeechModel>>
    fun observeModel(modelId: String): Flow<SpeechModel?>
    fun observeDownloadProgress(modelId: String): Flow<DownloadProgress?>
    suspend fun getModels(): List<SpeechModel>
    suspend fun getModel(modelId: String): SpeechModel?
    suspend fun getModelsByEngine(engineType: EngineType): List<SpeechModel>
    suspend fun downloadModel(modelId: String): Result<Unit>
    suspend fun pauseDownload(modelId: String)
    suspend fun resumeDownload(modelId: String)
    suspend fun cancelDownload(modelId: String)
    suspend fun deleteModel(modelId: String)
    suspend fun verifyModel(modelId: String): Boolean
    suspend fun activateModel(modelId: String)
    suspend fun getActiveModel(): SpeechModel?
    suspend fun getDefaultModels(): List<SpeechModel>
    suspend fun seedDefaultModelsIfEmpty()
    suspend fun refreshModelRegistry(): Result<Unit>
    suspend fun getExtractionProgress(modelId: String): Float
}
