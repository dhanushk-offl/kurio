package com.dhanu.kurio.data.audio

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import androidx.core.content.ContextCompat
import io.github.aakira.napier.Napier
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.io.ByteArrayOutputStream
import java.util.concurrent.ConcurrentLinkedQueue

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

    // VAD frame queue: frames are enqueued by the recording loop and
    // consumed by the VAD processor via readAudioFrame()
    private val frameQueue = ConcurrentLinkedQueue<ShortArray>()
    private val frameSize = 512 // 32ms frames at 16kHz

    // Ring buffer VAD state
    private val _vadAvailable = MutableStateFlow(false)
    val vadAvailable = _vadAvailable.asStateFlow()

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
                    bufferSize * 4 // larger buffer to avoid underrun
                )

                if (audioRecord?.state != AudioRecord.STATE_INITIALIZED) {
                    throw IllegalStateException("AudioRecord failed to initialise")
                }

                audioRecord?.startRecording()
                isRecording = true

                synchronized(outputStream) { outputStream.reset() }
                frameQueue.clear()

                recordingJob = CoroutineScope(Dispatchers.IO).launch {
                    val pcmBuffer = ByteArray(bufferSize)
                    val shortBuffer = ShortArray(frameSize)

                    while (isActive && isRecording) {
                        val bytesRead = audioRecord?.read(pcmBuffer, 0, pcmBuffer.size) ?: -1
                        if (bytesRead > 0) {
                            // Write to main audio buffer (for STT)
                            synchronized(outputStream) { outputStream.write(pcmBuffer, 0, bytesRead) }

                            // Enqueue frames for VAD (16-bit PCM → ShortArray)
                            var offset = 0
                            while (offset + frameSize * 2 <= bytesRead) {
                                for (i in 0 until frameSize) {
                                    val lo = pcmBuffer[offset + i * 2].toInt() and 0xFF
                                    val hi = pcmBuffer[offset + i * 2 + 1].toInt() shl 8
                                    shortBuffer[i] = (hi or lo).toShort()
                                }
                                frameQueue.offer(shortBuffer.copyOf())
                                offset += frameSize * 2
                            }
                            _vadAvailable.value = frameQueue.isNotEmpty()
                        } else if (bytesRead < 0) {
                            Napier.e(tag = "AudioRecorder") { "Error reading audio: $bytesRead" }
                            break
                        }
                    }
                }

                // Start foreground service
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
                    Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to start foreground service" }
                }

                Napier.d(tag = "AudioRecorder") { "Recording started at ${sampleRate}Hz with VAD frame queue" }
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to start recording: ${e.message}" }
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
                    Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to stop foreground service" }
                }

                frameQueue.clear()
                _vadAvailable.value = false

                val audioData = synchronized(outputStream) {
                    outputStream.toByteArray()
                }
                Napier.d(tag = "AudioRecorder") { "Recording stopped: ${audioData.size} bytes" }
                audioData
            } catch (e: Exception) {
                Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to stop recording: ${e.message}" }
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
                Napier.e(throwable = e, tag = "AudioRecorder") { "Failed to stop foreground service on cancel" }
            }

            synchronized(outputStream) { outputStream.reset() }
            frameQueue.clear()
            _vadAvailable.value = false
        }
    }

    override fun isRecording(): Boolean = isRecording

    override suspend fun readAudioFrame(frameSize: Int): ShortArray? {
        return withContext(Dispatchers.Default) {
            if (frameQueue.isEmpty()) null
            else frameQueue.poll()
        }
    }
}
