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
import kotlinx.coroutines.io.writeFully
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import java.security.MessageDigest

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
            val file = File(modelsDir, "${model.id}.bin")

            modelDao.updateStatus(modelId, ModelStatus.DOWNLOADING.name)

            val job = CoroutineScope(Dispatchers.IO).launch {
                try {
                    val response = api.downloadModel(model.downloadUrl)
                    val totalBytes = response.size.toLong()
                    var downloaded = 0L
                    val chunkSize = 8192
                    val outputStream = file.outputStream()

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

                    outputStream.close()

                    modelDao.updateFilePath(modelId, file.absolutePath)
                    modelDao.updateStatus(modelId, ModelStatus.VERIFYING.name)

                    val isValid = verifyChecksum(file, model.checksum)
                    if (isValid) {
                        modelDao.updateStatus(modelId, ModelStatus.VERIFIED.name)
                    } else {
                        file.delete()
                        modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
                        modelDao.updateFilePath(modelId, null)
                    }
                } catch (e: Exception) {
                    Napier.e("ModelDownload", throwable = e) { "Download failed for $modelId" }
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
        modelDao.updateStatus(modelId, ModelStatus.NOT_DOWNLOADED.name)
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
        return verifyChecksum(file, model.checksum)
    }

    override suspend fun activateModel(modelId: String) {
        val currentActive = modelDao.getActive()
        if (currentActive != null) {
            modelDao.updateStatus(currentActive.id, ModelStatus.DOWNLOADED.name)
        }
        modelDao.updateStatus(modelId, ModelStatus.ACTIVE.name)
    }

    override suspend fun getActiveModel(): SpeechModel? {
        return modelDao.getActive()?.toDomain()
    }

    override suspend fun getDefaultModels(): List<SpeechModel> {
        return listOf(
            SpeechModel(
                id = "whisper-tiny-en",
                name = "Whisper Tiny English",
                description = "Lightweight English-only model for fast transcription",
                provider = "OpenAI",
                license = "MIT",
                language = "English",
                sizeBytes = 39_000_000,
                ramUsageMb = 256,
                speedRating = 5,
                accuracyRating = 3,
                downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.en.bin",
                checksum = "",
                version = "1.0.0",
                recommendedDeviceClass = DeviceClass.LOW_END,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = false
            ),
            SpeechModel(
                id = "whisper-tiny",
                name = "Whisper Tiny Multilingual",
                description = "Multilingual model supporting multiple languages",
                provider = "OpenAI",
                license = "MIT",
                language = "Multilingual",
                sizeBytes = 75_000_000,
                ramUsageMb = 390,
                speedRating = 4,
                accuracyRating = 4,
                downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-tiny.bin",
                checksum = "",
                version = "1.0.0",
                recommendedDeviceClass = DeviceClass.MID_RANGE,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = false
            ),
            SpeechModel(
                id = "whisper-base-en",
                name = "Whisper Base English",
                description = "More accurate English-only model",
                provider = "OpenAI",
                license = "MIT",
                language = "English",
                sizeBytes = 74_000_000,
                ramUsageMb = 500,
                speedRating = 3,
                accuracyRating = 4,
                downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.en.bin",
                checksum = "",
                version = "1.0.0",
                recommendedDeviceClass = DeviceClass.MID_RANGE,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = false
            ),
            SpeechModel(
                id = "whisper-base",
                name = "Whisper Base Multilingual",
                description = "Accurate multilingual transcription",
                provider = "OpenAI",
                license = "MIT",
                language = "Multilingual",
                sizeBytes = 145_000_000,
                ramUsageMb = 700,
                speedRating = 2,
                accuracyRating = 5,
                downloadUrl = "https://huggingface.co/ggerganov/whisper.cpp/resolve/main/ggml-base.bin",
                checksum = "",
                version = "1.0.0",
                recommendedDeviceClass = DeviceClass.HIGH_END,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = false
            ),
            SpeechModel(
                id = "moonshine-tiny",
                name = "Moonshine Tiny",
                description = "Experimental ultra-fast mobile transcription model",
                provider = "Useful Sensors",
                license = "Apache-2.0",
                language = "English",
                sizeBytes = 25_000_000,
                ramUsageMb = 128,
                speedRating = 5,
                accuracyRating = 3,
                downloadUrl = "",
                checksum = "",
                version = "0.1.0",
                recommendedDeviceClass = DeviceClass.LOW_END,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = true
            ),
            SpeechModel(
                id = "moonshine-base",
                name = "Moonshine Base",
                description = "Experimental improved accuracy model",
                provider = "Useful Sensors",
                license = "Apache-2.0",
                language = "English",
                sizeBytes = 50_000_000,
                ramUsageMb = 256,
                speedRating = 4,
                accuracyRating = 4,
                downloadUrl = "",
                checksum = "",
                version = "0.1.0",
                recommendedDeviceClass = DeviceClass.MID_RANGE,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = true
            ),
            SpeechModel(
                id = "vosk-small-en",
                name = "Vosk Small English",
                description = "Very low-end device optimized model",
                provider = "Alpha Cephei",
                license = "Apache-2.0",
                language = "English",
                sizeBytes = 20_000_000,
                ramUsageMb = 96,
                speedRating = 5,
                accuracyRating = 2,
                downloadUrl = "",
                checksum = "",
                version = "0.3.0",
                recommendedDeviceClass = DeviceClass.LOW_END,
                status = ModelStatus.NOT_DOWNLOADED,
                isExperimental = true
            )
        )
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
                    status = ModelStatus.NOT_DOWNLOADED.name,
                    filePath = null,
                    isExperimental = dto.isExperimental
                )
            }
            modelDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e("ModelRegistry", throwable = e) { "Failed to refresh model registry" }
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

    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        if (expectedChecksum.isEmpty()) return true
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            val fileBytes = file.readBytes()
            val hash = digest.digest(fileBytes)
            val hexString = hash.joinToString("") { "%02x".format(it) }
            hexString == expectedChecksum
        } catch (e: Exception) {
            Napier.e("Checksum", throwable = e) { "Checksum verification failed" }
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
