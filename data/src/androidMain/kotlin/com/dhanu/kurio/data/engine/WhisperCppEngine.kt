package com.dhanu.kurio.data.engine

import android.content.Context
import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.util.IdGenerator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

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
            } catch (e: UnsatisfiedLinkError) {
                Napier.e(tag = "WhisperEngine") { "Native whisper library not available" }
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
                    Napier.w(tag = "WhisperEngine") { "Native lib not loaded, returning mock" }
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
                    modelId = currentModelPath?.substringAfterLast("/")?.substringBefore(".bin") ?: "unknown"
                )
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "WhisperEngine") { "Transcription failed" }
                null
            }
        }
    }

    override suspend fun loadModel(modelPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                if (nativeLoaded) {
                    modelLoaded = nativeInit(modelPath)
                } else {
                    modelLoaded = true
                }
                if (modelLoaded) {
                    currentModelPath = modelPath
                    Napier.d(tag = "WhisperEngine") { "Model loaded: $modelPath" }
                }
                modelLoaded
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "WhisperEngine") { "Failed to load model" }
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

    private fun simulateTranscription(audioData: ByteArray): String {
        return "This is a simulated transcription result. The native Whisper library is not loaded."
    }
}
