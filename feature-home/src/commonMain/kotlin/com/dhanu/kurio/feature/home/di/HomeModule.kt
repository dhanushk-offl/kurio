package com.dhanu.kurio.feature.home.di

import com.dhanu.kurio.feature.home.viewmodel.HomeViewModel
import org.koin.dsl.module

val homeModule = module {
    factory { HomeViewModel(get(), get(), get(), get(), get()) }
}
