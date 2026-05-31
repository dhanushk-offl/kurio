package com.dhanu.kurio.data.audio

import android.Manifest
import android.content.Context
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.os.Build
import io.github.aakira.napier.Napier
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    override suspend fun startRecording() {
        withContext(Dispatchers.IO) {
            try {
                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)

                audioRecord = AudioRecord(
                    MediaRecorder.AudioSource.MIC,
                    sampleRate,
                    channelConfig,
                    audioFormat,
                    bufferSize
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord failed to initialize")
                }

                audioRecord?.startRecording()
                isRecording = true

                Napier.d("AudioRecorder") { "Recording started at ${sampleRate}Hz" }
            } catch (e: Exception) {
                Napier.e("AudioRecorder", throwable = e) { "Failed to start recording" }
                throw e
            }
        }
    }

    override suspend fun stopRecording(): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                audioRecord?.stop()
                isRecording = false

                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                val buffer = ByteArray(bufferSize)
                val outputStream = ByteArrayOutputStream()

                audioRecord?.read(buffer, 0, buffer.size)
                if (buffer.isNotEmpty()) {
                    outputStream.write(buffer)
                }

                audioRecord?.release()
                audioRecord = null

                val audioData = outputStream.toByteArray()
                Napier.d("AudioRecorder") { "Recording stopped: ${audioData.size} bytes" }
                audioData
            } catch (e: Exception) {
                Napier.e("AudioRecorder", throwable = e) { "Failed to stop recording" }
                audioRecord?.release()
                audioRecord = null
                isRecording = false
                throw e
            }
        }
    }

    override suspend fun cancelRecording() {
        withContext(Dispatchers.IO) {
            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (_: Exception) { }
            audioRecord = null
            isRecording = false
        }
    }

    override fun isRecording(): Boolean = isRecording
}
