package com.dhanu.kurio.domain.usecase

import com.dhanu.kurio.domain.repository.TranscriptionRepository
import com.dhanu.kurio.domain.usecase.transcription.StartRecordingUseCase
import kotlin.test.Test
import kotlin.test.assertNull
import kotlinx.coroutines.test.runTest

class StartRecordingUseCaseTest {

    @Test
    fun `given repository returns null when startRecording then use case returns null`() = runTest {
        val useCase = StartRecordingUseCase(TranscriptionRepositoryMock(null))
        val result = useCase()
        assertNull(result)
    }
}

class TranscriptionRepositoryMock(
    private val error: com.dhanu.kurio.core.model.TranscriptionError?
) : TranscriptionRepository {
    override fun observeTranscriptionState() = kotlinx.coroutines.flow.flowOf(
        com.dhanu.kurio.core.model.TranscriptionState.IDLE
    )

    override fun observeCurrentResult() = kotlinx.coroutines.flow.flowOf(null)

    override suspend fun startRecording(): com.dhanu.kurio.core.model.TranscriptionError? = error

    override suspend fun stopRecording(): com.dhanu.kurio.core.model.TranscriptionResult? = null

    override suspend fun cancelRecording() {}

    override suspend fun getCurrentResult(): com.dhanu.kurio.core.model.TranscriptionResult? = null
}
