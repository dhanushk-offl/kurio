package com.dhanu.kurio.feature.transcription.viewmodel

import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.core.util.TimeUtils
import com.dhanu.kurio.domain.usecase.history.AddHistoryEntryUseCase
import com.dhanu.kurio.domain.usecase.settings.GetPreferencesUseCase
import com.dhanu.kurio.domain.usecase.transcription.StartRecordingUseCase
import com.dhanu.kurio.domain.usecase.transcription.StopRecordingUseCase
import com.dhanu.kurio.domain.usecase.transcription.ObserveTranscriptionStateUseCase
import com.dhanu.kurio.domain.usecase.transcription.ObserveCurrentResultUseCase
import com.dhanu.kurio.core.model.HistoryEntry
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class TranscriptionUiState(
    val state: TranscriptionState = TranscriptionState.IDLE,
    val result: TranscriptionResult? = null,
    val audioAmplitude: Float = 0f,
    val elapsedSeconds: Int = 0,
    val error: String? = null
)

class TranscriptionViewModel(
    private val startRecording: StartRecordingUseCase,
    private val stopRecording: StopRecordingUseCase,
    private val observeState: ObserveTranscriptionStateUseCase,
    private val observeResult: ObserveCurrentResultUseCase,
    private val getPreferences: GetPreferencesUseCase,
    private val addHistoryEntry: AddHistoryEntryUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(TranscriptionUiState())
    val uiState: StateFlow<TranscriptionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    fun initialize() {
        scope.launch {
            observeState().collect { state ->
                _uiState.update { it.copy(state = state) }
                when (state) {
                    TranscriptionState.LISTENING,
                    TranscriptionState.VAD_DETECTING,
                    TranscriptionState.VAD_SPEECH -> startTimer()
                    TranscriptionState.IDLE -> stopTimer()
                    else -> {}
                }
            }
        }
        scope.launch {
            observeResult().collect { result ->
                _uiState.update { it.copy(result = result) }
            }
        }
    }

    fun onStartRecording() {
        scope.launch {
            val error = startRecording()
            if (error != null) {
                _uiState.update { it.copy(error = error.message) }
            }
        }
    }

    fun onStopRecording() {
        scope.launch {
            val result = stopRecording()
            if (result != null) {
                val prefs = getPreferences()
                if (prefs.autoCopyEnabled) {
                    copyToClipboard(result.text)
                }
                addHistoryEntry(
                    HistoryEntry(
                        id = result.id,
                        text = result.text,
                        dateMillis = TimeUtils.now(),
                        durationMs = result.durationMs,
                        wordCount = result.wordCount,
                        characterCount = result.characterCount,
                        modelId = result.modelId
                    )
                )
            }
        }
    }

    fun onCancel() {
        scope.launch {
            stopTimer()
            _uiState.update { TranscriptionUiState() }
        }
    }

    fun onDismissError() {
        _uiState.update { it.copy(error = null) }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = scope.launch {
            var seconds = 0
            while (true) {
                kotlinx.coroutines.delay(1000)
                seconds++
                _uiState.update { it.copy(elapsedSeconds = seconds) }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun copyToClipboard(text: String) {
    }

    fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }
}
