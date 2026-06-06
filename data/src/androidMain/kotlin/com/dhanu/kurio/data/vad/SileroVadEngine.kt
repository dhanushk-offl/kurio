package com.dhanu.kurio.data.vad

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.nio.FloatBuffer

class SileroVadEngine(
    private val config: VadConfig = VadConfig()
) : VadEngine {

    @Volatile private var isLoaded = false
    private var ortEnv: OrtEnvironment? = null
    private var ortSession: OrtSession? = null

    // Silero VAD state h / c (each is 2 x 1 x 64)
    private var h: Array<FloatArray>? = null
    private var c: Array<FloatArray>? = null

    private var speechStartFrame = -1
    private var triggered = false
    private var currentSpeech = false
    private var pendingFrames = mutableListOf<Boolean>()

    override suspend fun loadModel(modelPath: String?): Boolean = withContext(Dispatchers.IO) {
        try {
            if (modelPath == null || !File(modelPath).exists()) {
                Napier.w(tag = "SileroVAD") { "Model not found at $modelPath" }
                return@withContext false
            }

            ortEnv = OrtEnvironment.getEnvironment()
            val sessionOptions = OrtSession.SessionOptions()
            sessionOptions.setIntraOpNumThreads(1)
            ortSession = ortEnv?.createSession(modelPath, sessionOptions)

            resetState()
            isLoaded = true
            Napier.d(tag = "SileroVAD") { "Silero VAD model loaded from $modelPath" }
            true
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "SileroVAD") { "Failed to load Silero VAD model" }
            isLoaded = false
            false
        }
    }

    override suspend fun isSpeech(audioFrame: ShortArray): Boolean = withContext(Dispatchers.Default) {
        if (!isLoaded || ortEnv == null || ortSession == null) return@withContext false

        try {
            // Convert 16-bit PCM to float32 and normalise to [-1, 1]
            val floatSamples = FloatArray(audioFrame.size) { audioFrame[it].toFloat() / 32768f }

            val env = ortEnv!!
            val session = ortSession!!

            // Build input tensor: shape [1, N]
            val shape = longArrayOf(1, floatSamples.size.toLong())
            val tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(floatSamples), shape)

            // State inputs h and c
            val hTensor = OnnxTensor.createTensor(env, h?.let { flatten2d(it) } ?: FloatArray(128))
            val cTensor = OnnxTensor.createTensor(env, c?.let { flatten2d(it) } ?: FloatArray(128))
            val srTensor = OnnxTensor.createTensor(env, longArrayOf(config.sampleRate.toLong()))

            val inputs = mapOf(
                "input" to tensor,
                "h" to hTensor,
                "c" to cTensor,
                "sr" to srTensor
            )

            val output = session.run(inputs)

            // Extract probability from output
            val outputTensor = output.get(0) as OnnxTensor
            val probability = outputTensor.floatBuffer.get()

            // Extract updated state
            val hOut = output.get(1) as OnnxTensor
            val cOut = output.get(2) as OnnxTensor
            h = unflatten2d(hOut.floatBuffer.array(), 2, 64)
            c = unflatten2d(cOut.floatBuffer.array(), 2, 64)

            // Cleanup
            tensor.close()
            hTensor.close()
            cTensor.close()
            srTensor.close()
            output.close()

            val isSpeech = probability > config.threshold

            // Apply hangover / debounce logic
            if (isSpeech) {
                if (!triggered) {
                    triggered = true
                    speechStartFrame = 0
                }
                currentSpeech = true
                pendingFrames.clear()
            } else if (triggered) {
                pendingFrames.add(false)
                if (pendingFrames.size >= minSilenceFrames) {
                    triggered = false
                    currentSpeech = false
                    pendingFrames.clear()
                }
            }

            currentSpeech
        } catch (e: Exception) {
            Napier.e(throwable = e, tag = "SileroVAD") { "VAD inference failed" }
            false
        }
    }

    override suspend fun reset() {
        resetState()
        triggered = false
        currentSpeech = false
        speechStartFrame = -1
        pendingFrames.clear()
    }

    override suspend fun release() {
        try {
            ortSession?.close()
            ortEnv?.close()
        } catch (_: Exception) { }
        isLoaded = false
        ortSession = null
        ortEnv = null
        resetState()
    }

    override fun isLoaded(): Boolean = isLoaded

    private fun resetState() {
        h = Array(2) { FloatArray(64) }
        c = Array(2) { FloatArray(64) }
    }

    private fun flatten2d(arr: Array<FloatArray>): FloatArray {
        val rows = arr.size
        val cols = arr[0].size
        val result = FloatArray(rows * cols)
        for (i in 0 until rows) {
            System.arraycopy(arr[i], 0, result, i * cols, cols)
        }
        return result
    }

    private fun unflatten2d(arr: FloatArray, rows: Int, cols: Int): Array<FloatArray> {
        return Array(rows) { i ->
            FloatArray(cols) { j -> arr[i * cols + j] }
        }
    }

    companion object {
        private val minSilenceFrames = 10
    }
}
