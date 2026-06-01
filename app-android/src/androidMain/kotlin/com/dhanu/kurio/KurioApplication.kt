package com.dhanu.kurio

import android.app.Application
import com.dhanu.kurio.android.notification.KurioNotificationManager
import com.dhanu.kurio.core.di.coreModule
import com.dhanu.kurio.data.di.androidDataModule
import com.dhanu.kurio.data.di.dataModule
import com.dhanu.kurio.domain.di.domainModule
import com.dhanu.kurio.feature.home.di.homeModule
import com.dhanu.kurio.feature.history.di.historyModule
import com.dhanu.kurio.feature.models.di.modelsModule
import com.dhanu.kurio.feature.settings.di.settingsModule
import com.dhanu.kurio.feature.transcription.di.transcriptionModule
import io.github.aakira.napier.DebugAntilog
import io.github.aakira.napier.Napier
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class KurioApplication : Application() {
    override fun onCreate() {
        super.onCreate()

        Napier.base(DebugAntilog())

        KurioNotificationManager.createNotificationChannel(this)

        startKoin {
            androidContext(this@KurioApplication)
            modules(
                coreModule,
                domainModule,
                dataModule,
                androidDataModule,
                homeModule,
                transcriptionModule,
                historyModule,
                modelsModule,
                settingsModule
            )
        }
    }
}
