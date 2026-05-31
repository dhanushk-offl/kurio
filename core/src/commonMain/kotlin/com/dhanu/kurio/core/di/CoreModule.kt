package com.dhanu.kurio.core.di

import org.koin.core.module.Module
import org.koin.dsl.module

val coreModule: Module = module {
    single { com.dhanu.kurio.core.util.TimeUtils }
    single { com.dhanu.kurio.core.util.StorageUtils }
    single { com.dhanu.kurio.core.util.IdGenerator }
}
