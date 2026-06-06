package com.dhanu.kurio.data.vad

import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class SimpleVadEngine(
    private val config: VadConfig = VadConfig()
) : VadEngine {

    @Volatile private var isModelLoaded = false
    private var energyThreshold = 0.0
    private var isSpeechActive = false
    private var speechFrames = 0
    private var silenceFrames = 0
    private val minSpeechFrames = (config.minSpeechDurationMs * config.sampleRate / 1000 / config.frameSize).coerceAtLeast(1)
    private val minSilenceFrames = (config.minSilenceDurationMs * config.sampleRate / 1000 / config.frameSize).coerceAtLeast(1)

    override suspend fun loadModel(modelPath: String?): Boolean = withContext(Dispatchers.Default) {
        isModelLoaded = true
        Napier.d(tag = "SimpleVAD") { "Energy-based VAD ready" }
        true
    }

    override suspend fun isSpeech(audioFrame: ShortArray): Boolean = withContext(Dispatchers.Default) {
        if (!isModelLoaded) return@withContext false

        val rms = computeRms(audioFrame)
        val energy = rms * rms

        // Adaptive threshold: initialise from first frame
        if (energyThreshold == 0.0) {
            energyThreshold = energy * 1.5 + 0.01
        }

        // Smooth threshold with EMA
        energyThreshold = if (energy > energyThreshold) {
            energyThreshold * 0.9 + energy * 0.1
        } else {
            energyThreshold * 0.99 + energy * 0.01
        }

        val adaptiveThreshold = energyThreshold * config.threshold

        if (energy > adaptiveThreshold) {
            silenceFrames = 0
            speechFrames++
            if (speechFrames >= minSpeechFrames) {
                isSpeechActive = true
            }
        } else {
            speechFrames = 0
            if (isSpeechActive) {
                silenceFrames++
                if (silenceFrames >= minSilenceFrames) {
                    isSpeechActive = false
                }
            }
        }

        isSpeechActive
    }

    override suspend fun reset() {
        isSpeechActive = false
        speechFrames = 0
        silenceFrames = 0
        energyThreshold = 0.0
    }

    override suspend fun release() {
        isModelLoaded = false
        reset()
    }

    override fun isLoaded(): Boolean = isModelLoaded

    private fun computeRms(samples: ShortArray): Double {
        if (samples.isEmpty()) return 0.0
        var sumSq = 0.0
        for (s in samples) {
            val normalized = s.toDouble() / 32768.0
            sumSq += normalized * normalized
        }
        return kotlin.math.sqrt(sumSq / samples.size)
    }
}
