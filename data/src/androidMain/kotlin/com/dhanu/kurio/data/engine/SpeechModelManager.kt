package com.dhanu.kurio.data.engine

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import com.dhanu.kurio.core.model.EngineType
import com.dhanu.kurio.core.model.ModelFormat
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.domain.repository.ModelRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import java.io.File

enum class ModelUnloadTimeout(val label: String, val millis: Long) {
    NEVER("Never", -1L),
    FIVE_MINUTES("5 min", 300_000L),
    TEN_MINUTES("10 min", 600_000L),
    THIRTY_MINUTES("30 min", 1_800_000L),
    IMMEDIATELY("Immediately", 0L)
}

class SpeechModelManager(
    private val context: Context,
    private val speechEngine: SpeechEngine,
    private val modelRepository: ModelRepository,
    private val modelsDir: String
) : ComponentCallbacks2 {

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var idleTimeoutJob: Job? = null
    private var lastActivityTime = System.currentTimeMillis()
    private var unloadTimeout: ModelUnloadTimeout = ModelUnloadTimeout.NEVER

    init {
        context.registerComponentCallbacks(this)
        Napier.d(tag = "SpeechModelManager") { "Initialised" }
    }

    /**
     * Sets the model unload idle timeout.
     */
    fun setUnloadTimeout(timeout: ModelUnloadTimeout) {
        unloadTimeout = timeout
        if (timeout == ModelUnloadTimeout.NEVER || timeout == ModelUnloadTimeout.IMMEDIATELY) {
            idleTimeoutJob?.cancel()
            idleTimeoutJob = null
        } else {
            startIdleTimer()
        }
        Napier.d(tag = "SpeechModelManager") { "Unload timeout set to ${timeout.label}" }
    }

    /**
     * Records user activity to reset the idle timer.
     */
    fun recordActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    /**
     * Discovers custom Whisper .bin / .gguf models in the models directory
     * that are not already registered in the database.
     */
    suspend fun discoverCustomModels(): List<SpeechModel> {
        return withContext(Dispatchers.IO) {
            try {
                val modelDir = File(modelsDir)
                if (!modelDir.exists()) return@withContext emptyList()

                val knownIds = modelRepository.getModels().map { it.id }.toSet()
                val customModels = mutableListOf<SpeechModel>()

                modelDir.listFiles()?.forEach { file ->
                    if (file.isFile && file.extension in setOf("bin", "gguf")) {
                        val modelId = "custom-${file.nameWithoutExtension}"
                        if (modelId !in knownIds) {
                            val model = SpeechModel(
                                id = modelId,
                                name = file.nameWithoutExtension.replaceFirstChar { it.uppercase() },
                                description = "Custom model discovered in models directory",
                                provider = "Custom",
                                license = "Unknown",
                                language = "Unknown",
                                sizeBytes = file.length(),
                                ramUsageMb = 0,
                                speedRating = 3,
                                accuracyRating = 3,
                                downloadUrl = "",
                                checksum = "",
                                version = "1.0.0",
                                recommendedDeviceClass = com.dhanu.kurio.core.model.DeviceClass.MID_RANGE,
                                engineType = EngineType.WHISPER_CPP,
                                modelFormat = com.dhanu.kurio.core.model.ModelFormat.GGML_BIN,
                                status = com.dhanu.kurio.core.model.ModelStatus.INSTALLED,
                                isExperimental = true
                            )
                            customModels.add(model)
                            Napier.d(tag = "SpeechModelManager") { "Discovered custom model: ${file.name}" }
                        }
                    }
                }

                customModels
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "SpeechModelManager") { "Custom model discovery failed" }
                emptyList()
            }
        }
    }

    /**
     * Pre-loads the currently active speech model.
     */
    fun warmActiveModel() {
        managerScope.launch {
            try {
                val activeModel = modelRepository.getActiveModel()
                if (activeModel != null) {
                    val fallback = File(modelsDir, "${activeModel.id}.${activeModel.modelFormat.extension}").absolutePath
                    val modelPath = activeModel.filePath ?: fallback
                    val modelFile = if (activeModel.isDirectory) File(modelPath) else File(modelPath)
                    if (modelFile.exists()) {
                        Napier.d(tag = "SpeechModelManager") { "Warming up active model: ${activeModel.name}" }
                        val success = speechEngine.loadModel(modelFile.absolutePath)
                        if (success) {
                            Napier.d(tag = "SpeechModelManager") { "Active model preloaded successfully" }
                        } else {
                            Napier.w(tag = "SpeechModelManager") { "Failed to preload active model" }
                        }
                    } else {
                        Napier.i(tag = "SpeechModelManager") { "Active model not yet downloaded. Skipping warm-up." }
                    }
                } else {
                    Napier.i(tag = "SpeechModelManager") { "No active model selected." }
                }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "SpeechModelManager") { "Error during model warm-up: ${e.message}" }
            }
        }
    }

    /**
     * Switches the loaded model to the target model ID.
     */
    fun switchModel(modelId: String, onComplete: (Boolean) -> Unit = {}) {
        managerScope.launch {
            try {
                val targetModel = modelRepository.getModel(modelId)
                if (targetModel == null) {
                    Napier.e(tag = "SpeechModelManager") { "Cannot switch: model not found: $modelId" }
                    onComplete(false)
                    return@launch
                }

                val modelPath = targetModel.filePath ?: File(modelsDir, "${targetModel.id}.${targetModel.modelFormat.extension}").absolutePath
                val modelFile = if (targetModel.isDirectory) File(modelPath) else File(modelPath)
                if (!modelFile.exists()) {
                    Napier.e(tag = "SpeechModelManager") { "Cannot switch: model binary not found: $modelPath" }
                    onComplete(false)
                    return@launch
                }

                Napier.d(tag = "SpeechModelManager") { "Switching to ${targetModel.name}" }

                // Unload previous
                speechEngine.unloadModel()

                // Load new
                val success = speechEngine.loadModel(modelFile.absolutePath)
                if (success) {
                    modelRepository.activateModel(modelId)
                    Napier.d(tag = "SpeechModelManager") { "Loaded and activated: ${targetModel.name}" }
                } else {
                    Napier.e(tag = "SpeechModelManager") { "Failed to load: ${targetModel.name}" }
                }

                recordActivity()
                onComplete(success)
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "SpeechModelManager") { "Error during model switch: ${e.message}" }
                onComplete(false)
            }
        }
    }

    /**
     * Unloads the currently loaded model if it has been idle for longer than the timeout.
     */
    private fun startIdleTimer() {
        idleTimeoutJob?.cancel()
        idleTimeoutJob = managerScope.launch {
            while (isActive) {
                delay(10_000) // Check every 10 seconds
                if (unloadTimeout.millis <= 0 || !speechEngine.isModelLoaded()) continue

                val idleTime = System.currentTimeMillis() - lastActivityTime
                if (idleTime >= unloadTimeout.millis) {
                    Napier.d(tag = "SpeechModelManager") { "Idle timeout reached (${unloadTimeout.label}). Unloading model." }
                    speechEngine.unloadModel()
                }
            }
        }
    }

    // ── ComponentCallbacks2 ───────────────────────────────

    override fun onTrimMemory(level: Int) {
        Napier.d(tag = "SpeechModelManager") { "onTrimMemory: $level" }
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL ||
            level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE ||
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
        ) {
            Napier.w(tag = "SpeechModelManager") { "Memory pressure detected. Unloading model." }
            managerScope.launch { speechEngine.unloadModel() }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {}

    override fun onLowMemory() {
        Napier.w(tag = "SpeechModelManager") { "onLowMemory! Unloading model immediately." }
        managerScope.launch { speechEngine.unloadModel() }
    }

    fun destroy() {
        idleTimeoutJob?.cancel()
        try { context.unregisterComponentCallbacks(this) } catch (_: Exception) { }
    }
}
