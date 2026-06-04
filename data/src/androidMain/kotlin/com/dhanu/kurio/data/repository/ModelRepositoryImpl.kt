package com.dhanu.kurio.data.repository

import com.dhanu.kurio.core.model.DownloadProgress
import com.dhanu.kurio.core.model.ModelStatus
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.data.local.dao.ModelDao
import com.dhanu.kurio.data.local.entity.ModelEntity
import com.dhanu.kurio.data.remote.api.KurioApi
import com.dhanu.kurio.data.remote.dto.ModelDefinitionDto
import com.dhanu.kurio.domain.repository.ModelRepository
import com.dhanu.kurio.core.model.DeviceClass
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest
import kotlin.math.absoluteValue

class ModelRepositoryImpl(
    private val modelDao: ModelDao,
    private val api: KurioApi,
    private val modelsDir: String
) : ModelRepository {

    private val downloadJobs = mutableMapOf<String, Job>()
    private val downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())

    override fun observeModels(): Flow<List<SpeechModel>> {
        return modelDao.observeAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun observeModel(modelId: String): Flow<SpeechModel?> {
        return modelDao.observeById(modelId).map { it?.toDomain() }
    }

    override fun observeDownloadProgress(modelId: String): Flow<DownloadProgress?> {
        return downloadProgress.map { it[modelId] }
    }

    override suspend fun getModels(): List<SpeechModel> {
        return modelDao.getAll().map { it.toDomain() }
    }

    override suspend fun getModel(modelId: String): SpeechModel? {
        return modelDao.getById(modelId)?.toDomain()
    }

    override suspend fun downloadModel(modelId: String): Result<Unit> {
        return try {
            val model = modelDao.getById(modelId) ?: return Result.failure(Exception("Model not found"))
            if (model.downloadUrl.isBlank()) {
                modelDao.updateStatus(modelId, ModelStatus.ERROR.name)
                return Result.failure(Exception("Model download URL is not available"))
            }
            val file = File(modelsDir, "${model.id}.bin")

            modelDao.updateStatus(modelId, ModelStatus.DOWNLOADING.name)

            val job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.downloadModel(model.downloadUrl)
                    val totalBytes = response.size.toLong()
                    var downloaded = 0L
                    val chunkSize = 8192
                    file.outputStream().use { outputStream ->
                        response.inputStream().use { input ->
                        val buffer = ByteArray(chunkSize)
                        var bytesRead: Int
                        while (input.read(buffer).also { bytesRead = it } != -1) {
                            outputStream.write(buffer, 0, bytesRead)
                            downloaded += bytesRead
                            downloadProgress.value = downloadProgress.value + (modelId to DownloadProgress(
                                modelId = modelId,
                                bytesDownloaded = downloaded,
                                totalBytes = totalBytes,
                                speed = 0
                            ))
                        }
                    }
                    }

                    modelDao.updateFilePath(modelId, file.absolutePath)
                    modelDao.updateStatus(modelId, ModelStatus.VERIFYING.name)

                    val isValid = validateModelBinary(file, model)
                    if (isValid) {
                        modelDao.updateStatus(modelId, ModelStatus.INSTALLED.name)
                    } else {
                        file.delete()
                        modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
                        modelDao.updateFilePath(modelId, null)
                    }
                } catch (e: Exception) {
                    Napier.e(throwable = e, tag = "ModelDownload") { "Download failed for $modelId" }
                    modelDao.updateStatus(modelId, ModelStatus.ERROR.name)
                }
            }

            downloadJobs[modelId] = job
            job.join()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun pauseDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        modelDao.updateStatus(modelId, ModelStatus.PAUSED.name)
    }

    override suspend fun resumeDownload(modelId: String) {
        val model = modelDao.getById(modelId) ?: return
        modelDao.updateStatus(modelId, ModelStatus.DOWNLOADING.name)
        downloadModel(modelId)
    }

    override suspend fun cancelDownload(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        modelDao.updateStatus(modelId, ModelStatus.NOT_INSTALLED.name)
        val file = File(modelsDir, "${modelId}.bin")
        if (file.exists()) file.delete()
        modelDao.updateFilePath(modelId, null)
    }

    override suspend fun deleteModel(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        val model = modelDao.getById(modelId)
        model?.filePath?.let { path ->
            val file = File(path)
            if (file.exists()) file.delete()
        }
        modelDao.updateFilePath(modelId, null)
        modelDao.resetStatus(modelId)
    }

    override suspend fun verifyModel(modelId: String): Boolean {
        val model = modelDao.getById(modelId) ?: return false
        val file = model.filePath?.let { File(it) } ?: return false
        if (!file.exists()) return false
        return validateModelBinary(file, model)
    }

    override suspend fun activateModel(modelId: String) {
        val target = modelDao.getById(modelId) ?: return
        if (!validateModelBinary(target.filePath?.let(::File), target)) {
            modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
            return
        }
        val currentActive = modelDao.getActive()
        if (currentActive != null) {
            modelDao.updateStatus(currentActive.id, ModelStatus.IDLE.name)
        }
        modelDao.updateStatus(modelId, ModelStatus.ACTIVE.name)
    }

    override suspend fun getActiveModel(): SpeechModel? {
        return modelDao.getActive()?.toDomain()
    }

    override suspend fun getDefaultModels(): List<SpeechModel> {
        return com.dhanu.kurio.core.model.SpeechModelRegistry.getDefaultModels()
    }

    override suspend fun refreshModelRegistry(): Result<Unit> {
        return try {
            val registry = api.getModelRegistry()
            val entities = registry.models.map { dto ->
                ModelEntity(
                    id = dto.id,
                    name = dto.name,
                    description = dto.description,
                    provider = dto.provider,
                    license = dto.license,
                    language = dto.language,
                    sizeBytes = dto.sizeBytes,
                    ramUsageMb = dto.ramUsageMb,
                    speedRating = dto.speedRating,
                    accuracyRating = dto.accuracyRating,
                    downloadUrl = dto.downloadUrl,
                    checksum = dto.checksum,
                    version = dto.version,
                    recommendedDeviceClass = dto.recommendedDeviceClass,
                    status = ModelStatus.NOT_INSTALLED.name,
                    filePath = null,
                    isExperimental = dto.isExperimental
                )
            }
            modelDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "ModelRegistry") { "Failed to refresh model registry" }
            Result.failure(e)
        }
    }

    private suspend fun seedDefaultModels() {
        val existing = modelDao.getAll()
        if (existing.isEmpty()) {
            val defaults = getDefaultModels().map { ModelEntity.fromDomain(it) }
            modelDao.insertAll(defaults)
        }
    }

    private fun validateModelBinary(file: File?, model: ModelEntity): Boolean {
        if (file == null || !file.exists() || !file.isFile) {
            Napier.w(tag = "ModelValidation") { "Model file is missing for ${model.id}" }
            return false
        }
        if (!file.absolutePath.startsWith(File(modelsDir).absolutePath)) {
            Napier.w(tag = "ModelValidation") { "Model file is outside managed storage for ${model.id}" }
            return false
        }
        if (!isSupportedModelFile(file)) {
            Napier.w(tag = "ModelValidation") { "Unsupported model file type: ${file.name}" }
            return false
        }
        if (!hasPlausibleSize(file, model.sizeBytes)) {
            Napier.w(tag = "ModelValidation") { "Model size validation failed for ${model.id}" }
            return false
        }
        if (model.provider.isBlank() || model.version.isBlank()) {
            Napier.w(tag = "ModelValidation") { "Model metadata validation failed for ${model.id}" }
            return false
        }
        return verifyChecksum(file, model.checksum)
    }

    private fun isSupportedModelFile(file: File): Boolean {
        return file.extension.lowercase() in setOf("bin", "gguf")
    }

    private fun hasPlausibleSize(file: File, expectedSizeBytes: Long): Boolean {
        if (expectedSizeBytes <= 0L) return file.length() > 0L
        val deltaRatio = (file.length() - expectedSizeBytes).absoluteValue.toDouble() / expectedSizeBytes.toDouble()
        return deltaRatio <= 0.25
    }

    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        if (expectedChecksum.isBlank()) {
            Napier.w(tag = "Checksum") { "Checksum missing for ${file.name}; accepted only after size and metadata validation" }
            return true
        }
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }
            val hexString = digest.digest().joinToString("") { "%02x".format(it) }
            hexString == expectedChecksum
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "Checksum") { "Checksum verification failed" }
            false
        }
    }

    companion object {
        private operator fun <K, V> MutableMap<K, V>.plus(
            pair: Pair<K, V>
        ): MutableMap<K, V> {
            val result = this.toMutableMap()
            result[pair.first] = pair.second
            return result
        }
    }
}
