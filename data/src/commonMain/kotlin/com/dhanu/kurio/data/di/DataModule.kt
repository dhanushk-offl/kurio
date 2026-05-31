package com.dhanu.kurio.data.di

import com.dhanu.kurio.data.repository.HistoryRepositoryImpl
import com.dhanu.kurio.data.repository.TranscriptionRepositoryImpl
import com.dhanu.kurio.domain.repository.HistoryRepository
import com.dhanu.kurio.domain.repository.TranscriptionRepository
import org.koin.dsl.module

val dataModule = module {
    single<HistoryRepository> { HistoryRepositoryImpl(get()) }
    single<TranscriptionRepository> { TranscriptionRepositoryImpl(get(), get()) }
}
