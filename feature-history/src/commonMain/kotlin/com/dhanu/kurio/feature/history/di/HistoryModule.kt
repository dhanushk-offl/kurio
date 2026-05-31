package com.dhanu.kurio.feature.history.di

import com.dhanu.kurio.feature.history.viewmodel.HistoryViewModel
import org.koin.dsl.module

val historyModule = module {
    factory { HistoryViewModel(get(), get(), get(), get()) }
}
