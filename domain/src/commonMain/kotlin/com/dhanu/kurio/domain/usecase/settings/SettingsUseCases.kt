package com.dhanu.kurio.domain.usecase.settings

import com.dhanu.kurio.core.model.AppPreferences
import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.core.model.StorageUsage
import com.dhanu.kurio.core.model.UpdateCheckResult
import com.dhanu.kurio.domain.repository.SettingsRepository
import kotlinx.coroutines.flow.Flow

class ObservePreferencesUseCase(
    private val repository: SettingsRepository
) {
    operator fun invoke(): Flow<AppPreferences> = repository.observePreferences()
}

class GetPreferencesUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): AppPreferences = repository.getPreferences()
}

class UpdateAutoCopyUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAutoCopy(enabled)
}

class UpdateHapticFeedbackUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setHapticFeedback(enabled)
}

class UpdateDarkModeUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setDarkMode(enabled)
}

class UpdateAnalyticsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setAnalytics(enabled)
}

class UpdateNotificationsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(enabled: Boolean) = repository.setNotifications(enabled)
}

class UpdateReleaseChannelUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(channel: ReleaseChannel) = repository.setReleaseChannel(channel)
}

class UpdateSelectedModelUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(modelId: String) = repository.setSelectedModel(modelId)
}

class GetStorageUsageUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Flow<StorageUsage> = repository.getStorageUsage()
}

class ClearCacheUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke() = repository.clearCache()
}

class CheckForUpdatesUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): UpdateCheckResult = repository.checkForUpdates()
}

class DismissAnnouncementUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(id: String) = repository.dismissAnnouncement(id)
}

class GetDismissedAnnouncementsUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): Set<String> = repository.getDismissedAnnouncements()
}

class GetChangelogUseCase(
    private val repository: SettingsRepository
) {
    suspend operator fun invoke(): List<com.dhanu.kurio.core.model.AppVersion> = repository.getChangelog()
}
