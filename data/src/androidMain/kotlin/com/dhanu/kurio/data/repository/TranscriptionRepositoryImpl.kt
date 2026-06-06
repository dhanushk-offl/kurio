package com.dhanu.kurio.data.repository

import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.core.model.TranscriptionError
import com.dhanu.kurio.core.model.TranscriptionErrorCode
import com.dhanu.kurio.data.audio.AudioRecorder
import com.dhanu.kurio.data.engine.SpeechEngine
import com.dhanu.kurio.data.llm.PostProcessor
import com.dhanu.kurio.data.vad.VadEngine
import com.dhanu.kurio.domain.repository.TranscriptionRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TranscriptionRepositoryImpl(
    private val audioRecorder: AudioRecorder,
    private val speechEngine: SpeechEngine,
    private val vadEngine: VadEngine? = null,
    private val postProcessor: PostProcessor? = null
) : TranscriptionRepository {

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private val _state = MutableStateFlow(TranscriptionState.IDLE)
    override fun observeTranscriptionState(): Flow<TranscriptionState> = _state.asStateFlow()

    private val _currentResult = MutableStateFlow<TranscriptionResult?>(null)
    override fun observeCurrentResult(): Flow<TranscriptionResult?> = _currentResult.asStateFlow()

    private var audioBuffer: ByteArray? = null
    private var vadJob: Job? = null
    private var isVadActive = false

    override suspend fun startRecording(): TranscriptionError? {
        return try {
            _state.value = TranscriptionState.LISTENING
            audioRecorder.startRecording()

            // Start VAD in background if available
            if (vadEngine != null && vadEngine.isLoaded()) {
                startVadLoop()
            }

            null
        } catch (e: Exception) {
            _state.value = TranscriptionState.ERROR
            TranscriptionError(
                code = TranscriptionErrorCode.MICROPHONE_FAILURE,
                message = "Failed to start recording",
                details = e.message
            )
        }
    }

    override suspend fun stopRecording(): TranscriptionResult? {
        return try {
            // Stop VAD
            stopVadLoop()

            _state.value = TranscriptionState.PROCESSING
            val audioData = audioRecorder.stopRecording()
            audioBuffer = audioData

            if (audioData.isEmpty()) {
                _state.value = TranscriptionState.IDLE
                return null
            }

            // Run STT inference
            val result = speechEngine.transcribe(audioData)
            if (result == null || result.text.isBlank()) {
                _state.value = TranscriptionState.IDLE
                _currentResult.value = null
                return null
            }

            var finalResult = result.copy(vadProcessed = isVadActive)

            // Optional LLM post-processing
            if (postProcessor != null && postProcessor.isEnabled() && postProcessor.isLoaded()) {
                _state.value = TranscriptionState.POST_PROCESSING
                val processedText = postProcessor.process(result.text, result.language)
                finalResult = finalResult.copy(
                    text = processedText,
                    postProcessed = processedText != result.text
                )
            }

            _currentResult.value = finalResult
            _state.value = TranscriptionState.COMPLETED
            finalResult
        } catch (e: Exception) {
            _state.value = TranscriptionState.ERROR
            _currentResult.value = null
            Napier.e(throwable = e, tag = "Transcription") { "Transcription failed: ${e.message}" }
            null
        }
    }

    override suspend fun cancelRecording() {
        stopVadLoop()
        try {
            audioRecorder.cancelRecording()
        } catch (_: Exception) { }
        _state.value = TranscriptionState.IDLE
        audioBuffer = null
    }

    override suspend fun getCurrentResult(): TranscriptionResult? {
        return _currentResult.value
    }

    // ── VAD Loop ───────────────────────────────────────────

    private fun startVadLoop() {
        isVadActive = true
        vadJob = scope.launch {
            try {
                val frameSize = 512 // 32ms at 16kHz
                val buffer = ShortArray(frameSize)
                var speechDetected = false

                while (isActive && isVadActive) {
                    // Read audio frames from recorder and run VAD
                    val frame = audioRecorder.readAudioFrame(frameSize) ?: break
                    val isSpeech = vadEngine?.isSpeech(frame) ?: false

                    if (isSpeech && !speechDetected) {
                        speechDetected = true
                        _state.value = TranscriptionState.VAD_SPEECH
                        Napier.d(tag = "VAD") { "Speech started" }
                    } else if (!isSpeech && speechDetected) {
                        speechDetected = false
                        _state.value = TranscriptionState.LISTENING
                        Napier.d(tag = "VAD") { "Speech ended" }
                    }

                    delay(10) // Yield to prevent busy loop
                }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "VAD") { "VAD loop error" }
            }
        }
    }

    private fun stopVadLoop() {
        isVadActive = false
        vadJob?.cancel()
        vadJob = null
    }
}
