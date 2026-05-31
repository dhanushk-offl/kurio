package com.dhanu.kurio.feature.settings.di

import com.dhanu.kurio.feature.settings.viewmodel.SettingsViewModel
import org.koin.dsl.module

val settingsModule = module {
    factory { SettingsViewModel(get(), get(), get(), get(), get(), get(), get(), get(), get()) }
}
