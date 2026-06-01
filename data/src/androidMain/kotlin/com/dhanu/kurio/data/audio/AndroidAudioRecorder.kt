package com.dhanu.kurio.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class AndroidAudioRecorder(
    private val context: Context
) : AudioRecorder {

    private var audioRecord: AudioRecord? = null
    private var isRecording = false
    private var recordingJob: Job? = null
    private val outputStream = ByteArrayOutputStream()

    private val sampleRate = 16000
    private val channelConfig = AudioFormat.CHANNEL_IN_MONO
    private val audioFormat = AudioFormat.ENCODING_PCM_16BIT

    override suspend fun startRecording() {
        withContext(Dispatchers.IO) {
            try {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.RECORD_AUDIO)
                    != PackageManager.PERMISSION_GRANTED
                ) {
                    throw SecurityException("RECORD_AUDIO permission not granted")
                }

                val bufferSize = AudioRecord.getMinBufferSize(sampleRate, channelConfig, audioFormat)
                if (bufferSize == AudioRecord.ERROR || bufferSize == AudioRecord.ERROR_BAD_VALUE) {
                    throw IllegalStateException("Invalid buffer size for recording")
                }

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
                
                synchronized(outputStream) {
                    outputStream.reset()
                }

                // Start continuous background recording loop
                recordingJob = CoroutineScope(Dispatchers.IO).launch {
                    val buffer = ByteArray(bufferSize)
                    while (isActive && isRecording) {
                        val readBytes = audioRecord?.read(buffer, 0, buffer.size) ?: -1
                        if (readBytes > 0) {
                            synchronized(outputStream) {
                                outputStream.write(buffer, 0, readBytes)
                            }
                        } else if (readBytes < 0) {
                            Napier.e(tag = "AudioRecorder") { "Error reading audio data: $readBytes" }
                            break
                        }
                    }
                }

                // Start foreground service to protect recording process
                try {
                    val intent = android.content.Intent().apply {
                        component = android.content.ComponentName(
                            context.packageName,
                            "com.dhanu.kurio.android.service.KurioForegroundService"
                        )
                        action = "com.dhanu.kurio.service.START"
                    }
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                        context.startForegroundService(intent)
                    } else {
                        context.startService(intent)
                    }
                } catch (e: Exception) {
                    Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to start KurioForegroundService" }
                }

                Napier.d(tag = "AudioRecorder") { "Recording started continuously at ${sampleRate}Hz" }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to start recording" }
                isRecording = false
                throw e
            }
        }
    }

    override suspend fun stopRecording(): ByteArray {
        return withContext(Dispatchers.IO) {
            try {
                isRecording = false
                recordingJob?.cancel()
                recordingJob = null

                audioRecord?.stop()
                audioRecord?.release()
                audioRecord = null

                // Stop foreground service
                try {
                    val intent = android.content.Intent().apply {
                        component = android.content.ComponentName(
                            context.packageName,
                            "com.dhanu.kurio.android.service.KurioForegroundService"
                        )
                        action = "com.dhanu.kurio.service.STOP"
                    }
                    context.startService(intent)
                } catch (e: Exception) {
                    Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to stop KurioForegroundService" }
                }

                val audioData = synchronized(outputStream) {
                    outputStream.toByteArray()
                }
                Napier.d(tag = "AudioRecorder") { "Recording stopped: ${audioData.size} bytes" }
                audioData
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to stop recording" }
                audioRecord?.release()
                audioRecord = null
                isRecording = false
                recordingJob?.cancel()
                recordingJob = null
                throw e
            }
        }
    }

    override suspend fun cancelRecording() {
        withContext(Dispatchers.IO) {
            isRecording = false
            recordingJob?.cancel()
            recordingJob = null
            try {
                audioRecord?.stop()
                audioRecord?.release()
            } catch (_: Exception) { }
            audioRecord = null

            // Stop foreground service
            try {
                val intent = android.content.Intent().apply {
                    component = android.content.ComponentName(
                        context.packageName,
                        "com.dhanu.kurio.android.service.KurioForegroundService"
                    )
                    action = "com.dhanu.kurio.service.STOP"
                }
                context.startService(intent)
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to stop KurioForegroundService on cancel" }
            }

            synchronized(outputStream) {
                outputStream.reset()
            }
        }
    }

    override fun isRecording(): Boolean = isRecording
}
