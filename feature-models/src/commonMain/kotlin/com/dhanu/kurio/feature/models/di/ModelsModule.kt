package com.dhanu.kurio.feature.models.di

import com.dhanu.kurio.feature.models.viewmodel.ModelsViewModel
import org.koin.dsl.module

val modelsModule = module {
    factory { ModelsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
