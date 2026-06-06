package com.dhanu.kurio.data.repository

import com.dhanu.kurio.core.model.DownloadProgress
import com.dhanu.kurio.core.model.ModelFormat
import com.dhanu.kurio.core.model.ModelStatus
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.core.model.EngineType
import com.dhanu.kurio.data.local.dao.ModelDao
import com.dhanu.kurio.data.local.entity.ModelEntity
import com.dhanu.kurio.data.remote.api.KurioApi
import com.dhanu.kurio.domain.repository.ModelRepository
import io.github.aakira.napier.Napier
import io.ktor.utils.io.readAvailable
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import java.io.File
import java.io.FileOutputStream
import java.security.MessageDigest
import java.util.zip.GZIPInputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import kotlin.math.absoluteValue

class ModelRepositoryImpl(
    private val modelDao: ModelDao,
    private val api: KurioApi,
    private val modelsDir: String
) : ModelRepository {

    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val downloadJobs = mutableMapOf<String, Job>()
    private val cancelFlags = mutableMapOf<String, CancellationFlag>()
    private val downloadProgress = MutableStateFlow<Map<String, DownloadProgress>>(emptyMap())
    private val extractionProgress = MutableStateFlow<Map<String, Float>>(emptyMap())

    private class CancellationFlag {
        @Volatile var cancelled = false
    }

    // ── Seed default models on DB if empty ──────────────────

    override suspend fun seedDefaultModelsIfEmpty() {
        val existing = modelDao.getAll()
        if (existing.isNotEmpty()) return
        val defaults = com.dhanu.kurio.core.model.SpeechModelRegistry.getDefaultModels()
        val entities = defaults.map { ModelEntity.fromDomain(it) }
        modelDao.insertAll(entities)
        Napier.d(tag = "ModelSeed") { "Seeded ${entities.size} default models into database" }
    }

    // ── Observables ────────────────────────────────────────

    override fun observeModels(): Flow<List<SpeechModel>> {
        return modelDao.observeAll().map { entities -> entities.map { it.toDomain() } }
    }

    override fun observeModel(modelId: String): Flow<SpeechModel?> {
        return modelDao.observeById(modelId).map { it?.toDomain() }
    }

    override fun observeDownloadProgress(modelId: String): Flow<DownloadProgress?> {
        return downloadProgress.map { it[modelId] }
    }

    // ── Queries ────────────────────────────────────────────

    override suspend fun getModels(): List<SpeechModel> {
        return modelDao.getAll().map { it.toDomain() }
    }

    override suspend fun getModel(modelId: String): SpeechModel? {
        return modelDao.getById(modelId)?.toDomain()
    }

    override suspend fun getModelsByEngine(engineType: EngineType): List<SpeechModel> {
        return modelDao.getByEngineType(engineType.name).map { it.toDomain() }
    }

    // ── Download with HTTP Range Resume ────────────────────

    override suspend fun downloadModel(modelId: String): Result<Unit> {
        return try {
            val entity = modelDao.getById(modelId)
                ?: return Result.failure(Exception("Model not found"))
            if (entity.downloadUrl.isBlank()) {
                modelDao.updateStatus(modelId, ModelStatus.ERROR.name)
                return Result.failure(Exception("Model download URL is not available"))
            }

            modelDao.updateStatus(modelId, ModelStatus.DOWNLOADING.name)

            val isDirectoryModel = entity.isDirectory
            val fmt = try {
                ModelFormat.valueOf(entity.modelFormat).extension
            } catch (_: Exception) { "bin" }
            val archiveFmt = if (fmt == "tar.gz") "tar.gz" else fmt
            val partialFileName = if (isDirectoryModel) "${entity.id}.$archiveFmt.partial" else "${entity.id}.$fmt.partial"
            val finalFileName = if (isDirectoryModel) "${entity.id}.$archiveFmt" else "${entity.id}.$fmt"
            val partialFile = File(modelsDir, partialFileName)
            val tempArchive = File(modelsDir, finalFileName)

            val cancelFlag = CancellationFlag()
            cancelFlags[modelId] = cancelFlag

            val job = scope.launch {
                try {
                    var resumeFrom = 0L
                    if (partialFile.exists()) {
                        resumeFrom = partialFile.length()
                        Napier.d(tag = "ModelDownload") { "Resuming $modelId from byte $resumeFrom" }
                    }

                    var streamResult = api.downloadModelStreaming(
                        entity.downloadUrl,
                        resumeFrom.takeIf { it > 0 }
                    )
                    var channel = streamResult.channel
                    var contentLength = streamResult.contentLength ?: entity.sizeBytes
                    var totalBytes = if (resumeFrom > 0) (contentLength + resumeFrom) else contentLength

                    if (resumeFrom > 0 && streamResult.statusCode != 206) {
                        Napier.w(tag = "ModelDownload") { "Server doesn't support Range. Restarting $modelId" }
                        channel.cancel(null)
                        partialFile.delete()
                        resumeFrom = 0
                        streamResult = api.downloadModelStreaming(entity.downloadUrl, null)
                        channel = streamResult.channel
                        contentLength = streamResult.contentLength ?: entity.sizeBytes
                        totalBytes = contentLength
                    }

                    val outputStream = FileOutputStream(partialFile, resumeFrom > 0)
                    val buffer = ByteArray(8192)
                    var downloaded = resumeFrom
                    var lastEmitTime = 0L
                    var lastBytes = downloaded
                    val startTime = System.currentTimeMillis()

                    while (!channel.isClosedForRead && isActive) {
                        if (cancelFlag.cancelled) {
                            Napier.i(tag = "ModelDownload") { "Download cancelled for $modelId" }
                            outputStream.close()
                            channel.cancel(null)
                            return@launch
                        }

                        val bytesRead = channel.readAvailable(buffer, 0, buffer.size)
                        if (bytesRead == -1) break

                        outputStream.write(buffer, 0, bytesRead)
                        downloaded += bytesRead

                        val now = System.currentTimeMillis()
                        val speed = if (now > lastEmitTime && (now - startTime) > 0)
                            ((downloaded - lastBytes) * 1000L / (now - lastEmitTime)) else 0L

                        if (now - lastEmitTime >= 200) {
                            downloadProgress.value = downloadProgress.value + (modelId to DownloadProgress(
                                modelId = modelId,
                                bytesDownloaded = downloaded,
                                totalBytes = totalBytes,
                                speed = speed
                            ))
                            lastEmitTime = now
                            lastBytes = downloaded
                        }
                    }

                    outputStream.close()
                    channel.cancel(null)

                    if (cancelFlag.cancelled) return@launch

                    if (downloaded != totalBytes && totalBytes > 0) {
                        partialFile.delete()
                        modelDao.updateStatus(modelId, ModelStatus.ERROR.name)
                        Napier.e(tag = "ModelDownload") { "Size mismatch for $modelId: got $downloaded, expected $totalBytes" }
                        return@launch
                    }

                    partialFile.renameTo(tempArchive)
                    modelDao.updateStatus(modelId, ModelStatus.VERIFYING.name)

                    val isValid = validateFile(tempArchive, entity)
                    if (!isValid) {
                        tempArchive.delete()
                        modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
                        return@launch
                    }

                    if (isDirectoryModel) {
                        modelDao.updateStatus(modelId, ModelStatus.INSTALLED.name)
                        extractModel(tempArchive, entity)
                    } else {
                        val finalFile = File(modelsDir, "${entity.id}.$fmt")
                        tempArchive.renameTo(finalFile)
                        modelDao.updateFilePath(modelId, finalFile.absolutePath)
                        modelDao.updateStatus(modelId, ModelStatus.INSTALLED.name)
                    }
                } catch (e: Exception) {
                    Napier.e(throwable = e, tag = "ModelDownload") { "Download failed for $modelId: ${e.message}" }
                    modelDao.updateStatus(modelId, ModelStatus.ERROR.name)
                } finally {
                    cancelFlags.remove(modelId)
                    downloadJobs.remove(modelId)
                }
            }

            downloadJobs[modelId] = job
            job.join()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ── Extraction (tar.gz or zip) ─────────────────────────

    private suspend fun extractModel(archive: File, entity: ModelEntity) {
        val modelDir = File(modelsDir, entity.id)
        modelDir.mkdirs()
        extractionProgress.value = extractionProgress.value + (entity.id to 0f)

        try {
            val isZip = isZipArchive(archive)
            if (isZip) {
                extractZip(archive, modelDir, entity)
            } else {
                extractTarGz(archive, modelDir, entity)
            }

            modelDao.updateFilePathWithType(entity.id, modelDir.absolutePath, true)
            Napier.d(tag = "ModelExtraction") { "Extracted ${entity.id} to $modelDir" }
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "ModelExtraction") { "Extraction failed: ${e.message}" }
            modelDir.deleteRecursively()
            archive.delete()
            modelDao.updateStatus(entity.id, ModelStatus.ERROR.name)
        } finally {
            extractionProgress.value = extractionProgress.value + (entity.id to 1f)
            archive.delete()
        }
    }

    private fun isZipArchive(file: File): Boolean {
        return try {
            file.inputStream().use { input ->
                val header = ByteArray(4)
                input.read(header) == 4 && header[0] == 0x50.toByte() && header[1] == 0x4B.toByte()
            }
        } catch (_: Exception) { false }
    }

    private suspend fun extractTarGz(archive: File, modelDir: File, entity: ModelEntity) {
        val totalBytes = archive.length()
        var processedBytes = 0L

        GZIPInputStream(archive.inputStream()).use { gz ->
            val rawBlock = ByteArray(512)

            while (true) {
                val headerRead = readFully(gz, rawBlock, 512)
                if (headerRead < 512) break

                if (rawBlock.all { it == 0.toByte() }) {
                    gz.read(rawBlock, 0, 512)
                    break
                }

                val name = rawBlock.copyOfRange(0, 100).toString(Charsets.UTF_8).trimEnd('\u0000').trimEnd('/')
                val sizeStr = rawBlock.copyOfRange(124, 136).toString(Charsets.UTF_8).trimEnd('\u0000')
                val fileSize = sizeStr.toLongOrNull(8) ?: 0L
                val typeFlag = rawBlock[156].toInt().toChar()

                val targetFile = File(modelDir, name)

                if (typeFlag == '5' || name.endsWith('/')) {
                    targetFile.mkdirs()
                } else if (typeFlag == '0' || typeFlag == '\u0000') {
                    targetFile.parentFile?.mkdirs()
                    FileOutputStream(targetFile).use { fos ->
                        var remaining = fileSize
                        val buf = ByteArray(8192)
                        while (remaining > 0) {
                            val read = gz.read(buf, 0, buf.size.coerceAtMost(remaining.toInt()))
                            if (read <= 0) break
                            fos.write(buf, 0, read)
                            remaining -= read
                        }
                    }
                }

                val padding = (512 - (fileSize % 512)) % 512
                if (padding > 0) gz.skip(padding)

                processedBytes += 512L + fileSize + padding
                val progress = (processedBytes.toFloat() / totalBytes).coerceAtMost(1f)
                extractionProgress.value = extractionProgress.value + (entity.id to progress)
            }
        }
    }

    private suspend fun extractZip(archive: File, modelDir: File, entity: ModelEntity) {
        val totalBytes = archive.length()
        ZipInputStream(archive.inputStream()).use { zis ->
            var entry: ZipEntry? = zis.nextEntry
            var processedBytes = 0L
            while (entry != null) {
                val targetFile = File(modelDir, entry.name)
                if (entry.isDirectory) {
                    targetFile.mkdirs()
                } else {
                    targetFile.parentFile?.mkdirs()
                    FileOutputStream(targetFile).use { fos ->
                        val buf = ByteArray(8192)
                        var read: Int
                        while (zis.read(buf).also { read = it } != -1) {
                            fos.write(buf, 0, read)
                        }
                    }
                }
                zis.closeEntry()
                processedBytes += entry.compressedSize.coerceAtLeast(1L)
                val progress = (processedBytes.toFloat() / totalBytes).coerceAtMost(1f)
                extractionProgress.value = extractionProgress.value + (entity.id to progress)
                entry = zis.nextEntry
            }
        }
    }

    private fun readFully(input: java.io.InputStream, buffer: ByteArray, length: Int): Int {
        var total = 0
        while (total < length) {
            val read = input.read(buffer, total, length - total)
            if (read == -1) break
            total += read
        }
        return total
    }

    // ── Pause / Resume / Cancel ────────────────────────────

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
        cancelFlags[modelId]?.cancelled = true
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        cancelFlags.remove(modelId)
        modelDao.updateStatus(modelId, ModelStatus.NOT_INSTALLED.name)

        File(modelsDir).listFiles()?.forEach { f ->
            if (f.name.startsWith(modelId) && f.name.endsWith(".partial")) f.delete()
        }
        modelDao.updateFilePath(modelId, null)
        downloadProgress.value = downloadProgress.value - modelId
    }

    override suspend fun deleteModel(modelId: String) {
        downloadJobs[modelId]?.cancel()
        downloadJobs.remove(modelId)
        cancelFlags.remove(modelId)

        val entity = modelDao.getById(modelId)
        entity?.filePath?.let { path ->
            val file = File(path)
            if (file.exists()) {
                if (file.isDirectory) file.deleteRecursively() else file.delete()
            }
        }

        File(modelsDir).listFiles()?.forEach { f ->
            if (f.name.startsWith(modelId)) f.delete()
        }

        modelDao.updateFilePath(modelId, null)
        modelDao.resetStatus(modelId)
        downloadProgress.value = downloadProgress.value - modelId
    }

    // ── Verification ───────────────────────────────────────

    override suspend fun verifyModel(modelId: String): Boolean {
        val entity = modelDao.getById(modelId) ?: return false
        val file = entity.filePath?.let { File(it) } ?: return false
        if (!file.exists()) return false
        if (entity.isDirectory) return file.isDirectory && (file.listFiles()?.isNotEmpty() == true)
        return validateFile(file, entity)
    }

    override suspend fun activateModel(modelId: String) {
        val target = modelDao.getById(modelId) ?: return

        if (target.isDirectory) {
            val dir = target.filePath?.let { File(it) }
            if (dir == null || !dir.isDirectory) {
                modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
                return
            }
        } else {
            val file = target.filePath?.let(::File)
            if (file == null || !file.exists()) {
                modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
                return
            }
            if (!validateFile(file, target)) {
                modelDao.updateStatus(modelId, ModelStatus.CORRUPTED.name)
                return
            }
        }

        val currentActive = modelDao.getActive()
        if (currentActive != null) modelDao.updateStatus(currentActive.id, ModelStatus.IDLE.name)
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
                    id = dto.id, name = dto.name, description = dto.description,
                    provider = dto.provider, license = dto.license, language = dto.language,
                    sizeBytes = dto.sizeBytes, ramUsageMb = dto.ramUsageMb,
                    speedRating = dto.speedRating, accuracyRating = dto.accuracyRating,
                    downloadUrl = dto.downloadUrl, checksum = dto.checksum, version = dto.version,
                    recommendedDeviceClass = dto.recommendedDeviceClass,
                    engineType = dto.engineType, modelFormat = dto.modelFormat,
                    isDirectory = dto.isDirectory, status = ModelStatus.NOT_INSTALLED.name,
                    filePath = null, isExperimental = dto.isExperimental
                )
            }
            modelDao.insertAll(entities)
            Result.success(Unit)
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "ModelRegistry") { "Failed to refresh: ${e.message}" }
            Result.failure(e)
        }
    }

    override suspend fun getExtractionProgress(modelId: String): Float {
        return extractionProgress.value[modelId] ?: 0f
    }

    // ── Validation ─────────────────────────────────────────

    private fun validateFile(file: File, model: ModelEntity): Boolean {
        if (!file.exists() || !file.isFile) return false
        if (!file.absolutePath.startsWith(File(modelsDir).absolutePath)) return false
        if (!isSupportedFormat(file)) return false
        if (!hasPlausibleSize(file, model.sizeBytes)) return false
        return verifyChecksum(file, model.checksum)
    }

    private fun isSupportedFormat(file: File) =
        file.extension.lowercase() in setOf("bin", "gguf", "gz", "tar", "zip")

    private fun hasPlausibleSize(file: File, expectedSizeBytes: Long): Boolean {
        if (expectedSizeBytes <= 0L) return file.length() > 0L
        return (file.length() - expectedSizeBytes).absoluteValue.toDouble() / expectedSizeBytes.toDouble() <= 0.25
    }

    private fun verifyChecksum(file: File, expectedChecksum: String): Boolean {
        if (expectedChecksum.isBlank()) return true
        return try {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) digest.update(buffer, 0, bytesRead)
            }
            digest.digest().joinToString("") { "%02x".format(it) } == expectedChecksum
        } catch (e: Exception) {
            false
        }
    }

    private companion object {
        private operator fun <K, V> MutableMap<K, V>.plus(pair: Pair<K, V>): MutableMap<K, V> {
            val result = this.toMutableMap()
            result[pair.first] = pair.second
            return result
        }
        private operator fun <K, V> MutableMap<K, V>.minus(key: K): MutableMap<K, V> {
            val result = this.toMutableMap()
            result.remove(key)
            return result
        }
        private const val DEFAULT_BUFFER_SIZE = 8192
    }
}
