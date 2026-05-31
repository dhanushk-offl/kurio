package com.dhanu.kurio.data.engine

import com.dhanu.kurio.core.model.TranscriptionResult

interface SpeechEngine {
    suspend fun transcribe(audioData: ByteArray): TranscriptionResult?
    suspend fun loadModel(modelPath: String): Boolean
    suspend fun unloadModel()
    fun isModelLoaded(): Boolean
}
