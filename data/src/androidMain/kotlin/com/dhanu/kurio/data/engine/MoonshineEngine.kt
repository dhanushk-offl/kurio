package com.dhanu.kurio.data.engine

import android.content.Context
import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.util.IdGenerator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class MoonshineEngine(
    private val context: Context
) : SpeechEngine {

    private var modelLoaded = false
    private var currentModelPath: String? = null
    private var nativeLoaded = false

    init {
        try {
            System.loadLibrary("moonshine")
            nativeLoaded = true
            Napier.d(tag = "MoonshineEngine") { "Native moonshine library loaded" }
        } catch (e: UnsatisfiedLinkError) {
            Napier.e(tag = "MoonshineEngine") { "Native moonshine library not available: ${e.message}" }
        }
    }

    private external fun nativeInit(modelDir: String): Boolean
    private external fun nativeTranscribe(audioData: ByteArray): String
    private external fun nativeRelease()

    override suspend fun transcribe(audioData: ByteArray): TranscriptionResult? {
        return withContext(Dispatchers.IO) {
            try {
                if (!modelLoaded) {
                    Napier.e(tag = "MoonshineEngine") { "Model not loaded" }
                    return@withContext null
                }

                val startTime = System.currentTimeMillis()

                val text = if (nativeLoaded) {
                    nativeTranscribe(audioData)
                } else {
                    Napier.w(tag = "MoonshineEngine") { "Native lib not loaded, returning simulated result" }
                    simulateTranscription(audioData)
                }

                val duration = System.currentTimeMillis() - startTime
                if (text.isBlank()) return@withContext null

                TranscriptionResult(
                    id = IdGenerator.generate(),
                    text = text,
                    date = System.currentTimeMillis(),
                    durationMs = duration,
                    wordCount = text.split("\\s+".toRegex()).size,
                    characterCount = text.length,
                    modelId = currentModelPath?.substringAfterLast("/") ?: "moonshine"
                )
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "MoonshineEngine") { "Transcription failed: ${e.message}" }
                null
            }
        }
    }

    override suspend fun loadModel(modelPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val modelDir = File(modelPath)
                if (!modelDir.isDirectory) {
                    Napier.e(tag = "MoonshineEngine") { "Expected directory model at $modelPath" }
                    return@withContext false
                }

                if (nativeLoaded) {
                    modelLoaded = nativeInit(modelPath)
                } else {
                    modelLoaded = true
                }

                if (modelLoaded) {
                    currentModelPath = modelPath
                    Napier.d(tag = "MoonshineEngine") { "Moonshine model loaded from $modelPath" }
                }
                modelLoaded
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "MoonshineEngine") { "Failed to load model: ${e.message}" }
                modelLoaded = false
                false
            }
        }
    }

    override suspend fun unloadModel() {
        withContext(Dispatchers.IO) {
            try {
                if (nativeLoaded) nativeRelease()
            } catch (_: Exception) { }
            modelLoaded = false
            currentModelPath = null
        }
    }

    override fun isModelLoaded(): Boolean = modelLoaded

    override fun getEngineType(): EngineVariant = EngineVariant.MOONSHINE

    private fun simulateTranscription(audioData: ByteArray): String {
        val sizeKb = audioData.size / 1024
        return "Moonshine simulated transcription ($sizeKb KB). Install libmoonshine.so for real inference."
    }
}
