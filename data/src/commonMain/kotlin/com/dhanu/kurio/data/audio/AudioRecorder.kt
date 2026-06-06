package com.dhanu.kurio.data.audio

interface AudioRecorder {
    suspend fun startRecording()
    suspend fun stopRecording(): ByteArray
    suspend fun cancelRecording()
    fun isRecording(): Boolean
    suspend fun readAudioFrame(frameSize: Int): ShortArray?
}
