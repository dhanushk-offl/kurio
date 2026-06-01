package com.dhanu.kurio.data.engine

import android.content.ComponentCallbacks2
import android.content.Context
import android.content.res.Configuration
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.domain.repository.ModelRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class SpeechModelManager(
    private val context: Context,
    private val speechEngine: SpeechEngine,
    private val modelRepository: ModelRepository,
    private val modelsDir: String
) : ComponentCallbacks2 {

    private val managerScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

    init {
        context.registerComponentCallbacks(this)
        Napier.d(tag = "SpeechModelManager") { "SpeechModelManager initialized and registered callbacks" }
    }

    /**
     * Pre-loads the currently active speech model into memory to ensure zero startup latency.
     */
    fun warmActiveModel() {
        managerScope.launch {
            try {
                val activeModel = modelRepository.getActiveModel()
                if (activeModel != null) {
                    val modelFile = File(modelsDir, "${activeModel.id}.bin")
                    if (modelFile.exists()) {
                        Napier.d(tag = "SpeechModelManager") { "Warming up active model: ${activeModel.name}" }
                        val success = speechEngine.loadModel(modelFile.absolutePath)
                        if (success) {
                            Napier.d(tag = "SpeechModelManager") { "Active model successfully preloaded" }
                        } else {
                            Napier.w(tag = "SpeechModelManager") { "Failed to preload active model" }
                        }
                    } else {
                        Napier.i(tag = "SpeechModelManager") { "Active model binary not downloaded yet. Skipping warm-up." }
                    }
                } else {
                    Napier.i(tag = "SpeechModelManager") { "No active speech model selected in settings." }
                }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "SpeechModelManager") { "Error during active model warm-up" }
            }
        }
    }

    /**
     * Safely switches the loaded model to the target model ID.
     */
    fun switchModel(modelId: String, onComplete: (Boolean) -> Unit = {}) {
        managerScope.launch {
            try {
                val targetModel = modelRepository.getModel(modelId)
                if (targetModel == null) {
                    Napier.e(tag = "SpeechModelManager") { "Cannot switch. Target model not found: $modelId" }
                    onComplete(false)
                    return@launch
                }

                val modelFile = File(modelsDir, "$modelId.bin")
                if (!modelFile.exists()) {
                    Napier.e(tag = "SpeechModelManager") { "Cannot switch. Model binary does not exist on disk: ${modelFile.absolutePath}" }
                    onComplete(false)
                    return@launch
                }

                Napier.d(tag = "SpeechModelManager") { "Switching loaded model to ${targetModel.name}" }
                
                // Unload previous model
                speechEngine.unloadModel()
                
                // Load new model
                val success = speechEngine.loadModel(modelFile.absolutePath)
                if (success) {
                    modelRepository.activateModel(modelId)
                    Napier.d(tag = "SpeechModelManager") { "Successfully loaded and activated model: ${targetModel.name}" }
                } else {
                    Napier.e(tag = "SpeechModelManager") { "Failed to load model binary: ${targetModel.name}" }
                }
                
                onComplete(success)
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "SpeechModelManager") { "Error during model switch" }
                onComplete(false)
            }
        }
    }

    // ComponentCallbacks2 Implementation
    override fun onTrimMemory(level: Int) {
        Napier.d(tag = "SpeechModelManager") { "System trimMemory triggered with level: $level" }
        // If memory is low or critical, or we are in the background and RAM is claimed, unload the C++ model!
        if (level >= ComponentCallbacks2.TRIM_MEMORY_RUNNING_CRITICAL || 
            level >= ComponentCallbacks2.TRIM_MEMORY_MODERATE ||
            level >= ComponentCallbacks2.TRIM_MEMORY_BACKGROUND
        ) {
            Napier.w(tag = "SpeechModelManager") { "Memory pressure detected. Gracefully unloading model to free native heap." }
            managerScope.launch {
                speechEngine.unloadModel()
            }
        }
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        // No-op
    }

    override fun onLowMemory() {
        Napier.w(tag = "SpeechModelManager") { "System onLowMemory triggered! Unloading model immediately." }
        managerScope.launch {
            speechEngine.unloadModel()
        }
    }

    /**
     * Unregisters the manager from ComponentCallbacks when destroyed.
     */
    fun destroy() {
        try {
            context.unregisterComponentCallbacks(this)
        } catch (_: Exception) { }
    }
}
