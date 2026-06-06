package com.dhanu.kurio.data.vad

interface VadEngine {
    suspend fun loadModel(modelPath: String? = null): Boolean
    suspend fun isSpeech(audioFrame: ShortArray): Boolean
    suspend fun reset()
    suspend fun release()
    fun isLoaded(): Boolean
}

data class VadConfig(
    val sampleRate: Int = 16000,
    val frameSize: Int = 512,
    val threshold: Float = 0.5f,
    val minSpeechDurationMs: Long = 100,
    val minSilenceDurationMs: Long = 500,
    val preSpeechPadFrames: Int = 10,
    val activationThreshold: Float = 0.7f,
    val deactivationThreshold: Float = 0.3f
)
