package com.dhanu.kurio.data.engine

import android.content.Context
import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.util.IdGenerator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class WhisperCppEngine(
    private val context: Context
) : SpeechEngine {

    private var modelLoaded = false
    private var currentModelPath: String? = null

    companion object {
        private var nativeLoaded = false

        init {
            try {
                System.loadLibrary("whisper")
                nativeLoaded = true
                Napier.d(tag = "WhisperEngine") { "Native whisper library loaded successfully" }
            } catch (e: UnsatisfiedLinkError) {
                Napier.e(tag = "WhisperEngine") { "Native whisper library not available: ${e.message}" }
            }
        }
    }

    private external fun nativeInit(modelPath: String): Boolean
    private external fun nativeTranscribe(audioData: ByteArray): String
    private external fun nativeRelease()

    override suspend fun transcribe(audioData: ByteArray): TranscriptionResult? {
        return withContext(Dispatchers.IO) {
            try {
                if (!modelLoaded) {
                    Napier.e(tag = "WhisperEngine") { "Model not loaded" }
                    return@withContext null
                }

                val startTime = System.currentTimeMillis()

                val text = if (nativeLoaded) {
                    nativeTranscribe(audioData)
                } else {
                    Napier.w(tag = "WhisperEngine") { "Native lib not loaded, returning simulated result" }
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
                    modelId = currentModelPath?.substringAfterLast("/")?.substringBefore(".") ?: "unknown"
                )
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "WhisperEngine") { "Transcription failed: ${e.message}" }
                null
            }
        }
    }

    override suspend fun loadModel(modelPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (!File(modelPath).exists()) {
                    Napier.e(tag = "WhisperEngine") { "Model file not found: $modelPath" }
                    return@withContext false
                }

                if (nativeLoaded) {
                    modelLoaded = nativeInit(modelPath)
                } else {
                    // Fallback: accept the path for simulation
                    modelLoaded = true
                }

                if (modelLoaded) {
                    currentModelPath = modelPath
                    Napier.d(tag = "WhisperEngine") { "Model loaded: $modelPath" }
                }
                modelLoaded
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "WhisperEngine") { "Failed to load model: ${e.message}" }
                modelLoaded = false
                false
            }
        }
    }

    override suspend fun unloadModel() {
        withContext(Dispatchers.IO) {
            try {
                if (nativeLoaded) {
                    nativeRelease()
                }
            } catch (_: Exception) { }
            modelLoaded = false
            currentModelPath = null
        }
    }

    override fun isModelLoaded(): Boolean = modelLoaded

    override fun getEngineType(): EngineVariant = EngineVariant.WHISPER_CPP

    private fun simulateTranscription(audioData: ByteArray): String {
        val sizeKb = audioData.size / 1024
        return "Simulated transcription result ($sizeKb KB audio processed). Install libwhisper.so for real inference."
    }
}
