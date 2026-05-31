package com.dhanu.kurio.domain.usecase.transcription

import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.domain.repository.TranscriptionRepository
import kotlinx.coroutines.flow.Flow

class ObserveTranscriptionStateUseCase(
    private val repository: TranscriptionRepository
) {
    operator fun invoke(): Flow<TranscriptionState> = repository.observeTranscriptionState()
}

class ObserveCurrentResultUseCase(
    private val repository: TranscriptionRepository
) {
    operator fun invoke(): Flow<TranscriptionResult?> = repository.observeCurrentResult()
}
