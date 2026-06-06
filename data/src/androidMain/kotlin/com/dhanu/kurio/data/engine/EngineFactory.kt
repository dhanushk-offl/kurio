package com.dhanu.kurio.data.engine

import android.content.Context
import com.dhanu.kurio.core.model.EngineType
import io.github.aakira.napier.Napier

object EngineFactory {
    fun createEngine(
        engineType: EngineType,
        context: Context
    ): SpeechEngine {
        return when (engineType) {
            EngineType.WHISPER_CPP -> {
                Napier.d(tag = "EngineFactory") { "Creating WhisperCppEngine" }
                WhisperCppEngine(context)
            }
            EngineType.MOONSHINE -> {
                Napier.d(tag = "EngineFactory") { "Creating MoonshineEngine" }
                MoonshineEngine(context)
            }
            EngineType.VOSK -> {
                Napier.d(tag = "EngineFactory") { "Creating VoskEngine" }
                VoskEngine(context)
            }
            EngineType.SENSE_VOICE -> {
                Napier.d(tag = "EngineFactory") { "Creating SenseVoiceEngine (falls back to WhisperCpp)" }
                WhisperCppEngine(context)
            }
            EngineType.PARAKET -> {
                Napier.d(tag = "EngineFactory") { "Creating ParakeetEngine (falls back to WhisperCpp)" }
                WhisperCppEngine(context)
            }
            EngineType.GIGA_AM -> {
                Napier.d(tag = "EngineFactory") { "Creating GigaAMEngine (falls back to WhisperCpp)" }
                WhisperCppEngine(context)
            }
        }
    }
}
