package com.dhanu.kurio.domain.repository

import com.dhanu.kurio.core.model.AppPreferences
import com.dhanu.kurio.core.model.AppVersion
import com.dhanu.kurio.core.model.FeatureAnnouncement
import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.core.model.StorageUsage
import com.dhanu.kurio.core.model.UpdateCheckResult
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    fun observePreferences(): Flow<AppPreferences>
    suspend fun getPreferences(): AppPreferences
    suspend fun updatePreferences(preferences: AppPreferences)
    suspend fun setAutoCopy(enabled: Boolean)
    suspend fun setHapticFeedback(enabled: Boolean)
    suspend fun setDarkMode(enabled: Boolean)
    suspend fun setAnalytics(enabled: Boolean)
    suspend fun setNotifications(enabled: Boolean)
    suspend fun setReleaseChannel(channel: ReleaseChannel)
    suspend fun setSelectedModel(modelId: String)
    suspend fun getStorageUsage(): Flow<StorageUsage>
    suspend fun clearCache()
    suspend fun setOnboardingCompleted(completed: Boolean)
    suspend fun isOnboardingCompleted(): Boolean
    suspend fun checkForUpdates(): UpdateCheckResult
    suspend fun getDismissedAnnouncements(): Set<String>
    suspend fun dismissAnnouncement(id: String)
    suspend fun getLastUpdateCheck(): Long
    suspend fun setLastUpdateCheck(timestamp: Long)
    suspend fun getChangelog(): List<AppVersion>
}
