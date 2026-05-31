package com.dhanu.kurio.feature.transcription.di

import com.dhanu.kurio.feature.transcription.viewmodel.TranscriptionViewModel
import org.koin.dsl.module

val transcriptionModule = module {
    factory { TranscriptionViewModel(get(), get(), get(), get(), get(), get()) }
}
