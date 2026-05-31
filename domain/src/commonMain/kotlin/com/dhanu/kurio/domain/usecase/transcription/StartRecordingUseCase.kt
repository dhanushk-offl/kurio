package com.dhanu.kurio.domain.usecase.transcription

import com.dhanu.kurio.core.model.TranscriptionError
import com.dhanu.kurio.domain.repository.TranscriptionRepository

class StartRecordingUseCase(
    private val repository: TranscriptionRepository
) {
    suspend operator fun invoke(): TranscriptionError? {
        return repository.startRecording()
    }
}
