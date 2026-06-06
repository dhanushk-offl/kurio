package com.dhanu.kurio.data.engine

import android.content.Context
import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.util.IdGenerator
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

class VoskEngine(
    private val context: Context
) : SpeechEngine {

    private var modelLoaded = false
    private var currentModelPath: String? = null
    private var nativeLoaded = false

    init {
        try {
            System.loadLibrary("vosk")
            nativeLoaded = true
            Napier.d(tag = "VoskEngine") { "Native vosk library loaded" }
        } catch (e: UnsatisfiedLinkError) {
            Napier.e(tag = "VoskEngine") { "Native vosk library not available: ${e.message}" }
        }
    }

    private external fun nativeInit(modelDir: String): Boolean
    private external fun nativeTranscribe(audioData: ByteArray): String
    private external fun nativeRelease()

    override suspend fun transcribe(audioData: ByteArray): TranscriptionResult? {
        return withContext(Dispatchers.IO) {
            try {
                if (!modelLoaded) {
                    Napier.e(tag = "VoskEngine") { "Model not loaded" }
                    return@withContext null
                }

                val startTime = System.currentTimeMillis()

                val text = if (nativeLoaded) {
                    nativeTranscribe(audioData)
                } else {
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
                    modelId = currentModelPath?.substringAfterLast("/") ?: "vosk"
                )
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "VoskEngine") { "Transcription failed: ${e.message}" }
                null
            }
        }
    }

    override suspend fun loadModel(modelPath: String): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val modelDir = File(modelPath)
                if (!modelDir.isDirectory) {
                    Napier.e(tag = "VoskEngine") { "Expected directory model at $modelPath" }
                    return@withContext false
                }

                if (nativeLoaded) {
                    modelLoaded = nativeInit(modelPath)
                } else {
                    modelLoaded = true
                }

                if (modelLoaded) {
                    currentModelPath = modelPath
                    Napier.d(tag = "VoskEngine") { "Vosk model loaded from $modelPath" }
                }
                modelLoaded
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "VoskEngine") { "Failed to load model: ${e.message}" }
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

    override fun getEngineType(): EngineVariant = EngineVariant.VOSK

    private fun simulateTranscription(audioData: ByteArray): String {
        val sizeKb = audioData.size / 1024
        return "Vosk simulated transcription ($sizeKb KB). Install libvosk.so for real inference."
    }
}
