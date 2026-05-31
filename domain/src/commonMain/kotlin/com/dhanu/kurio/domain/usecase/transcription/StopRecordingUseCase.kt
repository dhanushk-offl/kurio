package com.dhanu.kurio.domain.usecase.transcription

import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.domain.repository.TranscriptionRepository

class StopRecordingUseCase(
    private val repository: TranscriptionRepository
) {
    suspend operator fun invoke(): TranscriptionResult? {
        return repository.stopRecording()
    }
}
