package com.dhanu.kurio.domain.di

import com.dhanu.kurio.domain.usecase.history.*
import com.dhanu.kurio.domain.usecase.model.*
import com.dhanu.kurio.domain.usecase.settings.*
import com.dhanu.kurio.domain.usecase.transcription.*
import org.koin.dsl.module

val domainModule = module {
    factory { StartRecordingUseCase(get()) }
    factory { StopRecordingUseCase(get()) }
    factory { ObserveTranscriptionStateUseCase(get()) }
    factory { ObserveCurrentResultUseCase(get()) }

    factory { GetModelsUseCase(get()) }
    factory { GetModelUseCase(get()) }
    factory { DownloadModelUseCase(get()) }
    factory { PauseDownloadUseCase(get()) }
    factory { ResumeDownloadUseCase(get()) }
    factory { DeleteModelUseCase(get()) }
    factory { ActivateModelUseCase(get()) }
    factory { GetActiveModelUseCase(get()) }
    factory { ObserveDownloadProgressUseCase(get()) }

    factory { ObserveHistoryUseCase(get()) }
    factory { GetHistoryUseCase(get()) }
    factory { AddHistoryEntryUseCase(get()) }
    factory { DeleteHistoryEntryUseCase(get()) }
    factory { ClearHistoryUseCase(get()) }
    factory { SearchHistoryUseCase(get()) }
    factory { GetHistoryCountUseCase(get()) }

    factory { ObservePreferencesUseCase(get()) }
    factory { GetPreferencesUseCase(get()) }
    factory { UpdateAutoCopyUseCase(get()) }
    factory { UpdateHapticFeedbackUseCase(get()) }
    factory { UpdateDarkModeUseCase(get()) }
    factory { UpdateAnalyticsUseCase(get()) }
    factory { UpdateNotificationsUseCase(get()) }
    factory { UpdateReleaseChannelUseCase(get()) }
    factory { UpdateSelectedModelUseCase(get()) }
    factory { GetStorageUsageUseCase(get()) }
    factory { ClearCacheUseCase(get()) }
    factory { CheckForUpdatesUseCase(get()) }
    factory { DismissAnnouncementUseCase(get()) }
    factory { GetDismissedAnnouncementsUseCase(get()) }
    factory { GetChangelogUseCase(get()) }
}
