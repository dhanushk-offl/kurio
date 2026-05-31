package com.dhanu.kurio.domain.repository

import com.dhanu.kurio.core.model.TranscriptionResult
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.core.model.TranscriptionError
import kotlinx.coroutines.flow.Flow

interface TranscriptionRepository {
    fun observeTranscriptionState(): Flow<TranscriptionState>
    fun observeCurrentResult(): Flow<TranscriptionResult?>
    suspend fun startRecording(): TranscriptionError?
    suspend fun stopRecording(): TranscriptionResult?
    suspend fun cancelRecording()
    suspend fun getCurrentResult(): TranscriptionResult?
}
