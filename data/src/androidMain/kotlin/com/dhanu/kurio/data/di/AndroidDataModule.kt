package com.dhanu.kurio.data.di

import android.content.Context
import com.dhanu.kurio.data.audio.AudioRecorder
import com.dhanu.kurio.data.audio.AndroidAudioRecorder
import com.dhanu.kurio.data.createDatabase
import com.dhanu.kurio.data.engine.SpeechEngine
import com.dhanu.kurio.data.engine.WhisperCppEngine
import com.dhanu.kurio.data.remote.api.KurioApi
import com.dhanu.kurio.data.repository.ModelRepositoryImpl
import com.dhanu.kurio.data.repository.SettingsRepositoryImpl
import com.dhanu.kurio.domain.repository.ModelRepository
import com.dhanu.kurio.domain.repository.SettingsRepository
import io.ktor.client.*
import io.ktor.client.engine.okhttp.*
import io.ktor.client.plugins.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json
import org.koin.android.ext.koin.withContext
import org.koin.dsl.module

val androidDataModule = module {
    single { get<Context>().filesDir.resolve("models").also { it.mkdirs() }.absolutePath }

    single {
        HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(Json {
                    ignoreUnknownKeys = true
                    isLenient = true
                    prettyPrint = false
                })
            }
            install(HttpTimeout) {
                requestTimeoutMillis = 30_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 30_000
            }
        }
    }

    single { KurioApi(get()) }
    single { createDatabase(get()) }
    single { get<com.dhanu.kurio.data.local.KurioDatabase>().historyDao() }
    single { get<com.dhanu.kurio.data.local.KurioDatabase>().modelDao() }

    single<AudioRecorder> { AndroidAudioRecorder(get()) }
    single<SpeechEngine> { WhisperCppEngine(get()) }

    single<ModelRepository> { ModelRepositoryImpl(get(), get(), get()) }
    single<SettingsRepository> { SettingsRepositoryImpl(get(), get(), get(), get()) }
}
