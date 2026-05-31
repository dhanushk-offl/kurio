package com.dhanu.kurio.data.repository

import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.core.model.TranscriptionError
import com.dhanu.kurio.core.model.TranscriptionErrorCode
import com.dhanu.kurio.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class TranscriptionRepositoryImpl(
    private val audioRecorder: com.dhanu.kurio.data.audio.AudioRecorder,
    private val speechEngine: com.dhanu.kurio.data.engine.SpeechEngine
) : TranscriptionRepository {

    private val _state = MutableStateFlow(TranscriptionState.IDLE)
    override fun observeTranscriptionState(): Flow<TranscriptionState> = _state.asStateFlow()

    private val _currentResult = MutableStateFlow<TranscriptionResult?>(null)
    override fun observeCurrentResult(): Flow<TranscriptionResult?> = _currentResult.asStateFlow()

    private var audioBuffer: ByteArray? = null

    override suspend fun startRecording(): TranscriptionError? {
        return try {
            _state.value = TranscriptionState.LISTENING
            audioRecorder.startRecording()
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
            _state.value = TranscriptionState.PROCESSING
            val audioData = audioRecorder.stopRecording()
            audioBuffer = audioData

            val result = speechEngine.transcribe(audioData)
            _currentResult.value = result
            _state.value = TranscriptionState.COMPLETED
            result
        } catch (e: Exception) {
            _state.value = TranscriptionState.ERROR
            _currentResult.value = null
            null
        }
    }

    override suspend fun cancelRecording() {
        try {
            audioRecorder.cancelRecording()
        } catch (_: Exception) { }
        _state.value = TranscriptionState.IDLE
        audioBuffer = null
    }

    override suspend fun getCurrentResult(): TranscriptionResult? {
        return _currentResult.value
    }
}
