package com.dhanu.kurio.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.dhanu.kurio.core.model.AppPreferences
import com.dhanu.kurio.core.model.AppVersion
import com.dhanu.kurio.core.model.FeatureAnnouncement
import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.core.model.StorageUsage
import com.dhanu.kurio.core.model.UpdateCheckResult
import com.dhanu.kurio.data.local.dao.HistoryDao
import com.dhanu.kurio.data.local.dao.ModelDao
import com.dhanu.kurio.data.remote.api.KurioApi
import com.dhanu.kurio.data.dataStore
import com.dhanu.kurio.domain.repository.SettingsRepository
import io.github.aakira.napier.Napier
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.io.File

class SettingsRepositoryImpl(
    private val context: Context,
    private val api: KurioApi,
    private val historyDao: HistoryDao,
    private val modelDao: ModelDao
) : SettingsRepository {

    private companion object {
        val KEY_AUTO_COPY = booleanPreferencesKey("auto_copy")
        val KEY_HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
        val KEY_DARK_MODE = booleanPreferencesKey("dark_mode")
        val KEY_ANALYTICS = booleanPreferencesKey("analytics_enabled")
        val KEY_NOTIFICATIONS = booleanPreferencesKey("notifications_enabled")
        val KEY_RELEASE_CHANNEL = stringPreferencesKey("release_channel")
        val KEY_SELECTED_MODEL = stringPreferencesKey("selected_model")
        val KEY_ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
        val KEY_LAST_UPDATE_CHECK = longPreferencesKey("last_update_check")
        val KEY_DISMISSED_ANNOUNCEMENTS = stringPreferencesKey("dismissed_announcements")
    }

    override fun observePreferences(): Flow<AppPreferences> {
        return context.dataStore.data.map { prefs ->
            AppPreferences(
                autoCopyEnabled = prefs[KEY_AUTO_COPY] ?: true,
                hapticFeedbackEnabled = prefs[KEY_HAPTIC_FEEDBACK] ?: true,
                darkModeEnabled = prefs[KEY_DARK_MODE] ?: false,
                analyticsEnabled = prefs[KEY_ANALYTICS] ?: false,
                notificationsEnabled = prefs[KEY_NOTIFICATIONS] ?: true,
                releaseChannel = try {
                    ReleaseChannel.valueOf(prefs[KEY_RELEASE_CHANNEL] ?: "STABLE")
                } catch (_: Exception) { ReleaseChannel.STABLE },
                selectedModelId = prefs[KEY_SELECTED_MODEL] ?: "whisper-tiny-en",
                lastUpdateCheck = prefs[KEY_LAST_UPDATE_CHECK] ?: 0L
            )
        }
    }

    override suspend fun getPreferences(): AppPreferences {
        return observePreferences().first()
    }

    override suspend fun updatePreferences(preferences: AppPreferences) {
        context.dataStore.edit { prefs ->
            prefs[KEY_AUTO_COPY] = preferences.autoCopyEnabled
            prefs[KEY_HAPTIC_FEEDBACK] = preferences.hapticFeedbackEnabled
            prefs[KEY_DARK_MODE] = preferences.darkModeEnabled
            prefs[KEY_ANALYTICS] = preferences.analyticsEnabled
            prefs[KEY_NOTIFICATIONS] = preferences.notificationsEnabled
            prefs[KEY_RELEASE_CHANNEL] = preferences.releaseChannel.name
            prefs[KEY_SELECTED_MODEL] = preferences.selectedModelId
        }
    }

    override suspend fun setAutoCopy(enabled: Boolean) {
        context.dataStore.edit { it[KEY_AUTO_COPY] = enabled }
    }

    override suspend fun setHapticFeedback(enabled: Boolean) {
        context.dataStore.edit { it[KEY_HAPTIC_FEEDBACK] = enabled }
    }

    override suspend fun setDarkMode(enabled: Boolean) {
        context.dataStore.edit { it[KEY_DARK_MODE] = enabled }
    }

    override suspend fun setAnalytics(enabled: Boolean) {
        context.dataStore.edit { it[KEY_ANALYTICS] = enabled }
    }

    override suspend fun setNotifications(enabled: Boolean) {
        context.dataStore.edit { it[KEY_NOTIFICATIONS] = enabled }
    }

    override suspend fun setReleaseChannel(channel: ReleaseChannel) {
        context.dataStore.edit { it[KEY_RELEASE_CHANNEL] = channel.name }
    }

    override suspend fun setSelectedModel(modelId: String) {
        context.dataStore.edit { it[KEY_SELECTED_MODEL] = modelId }
    }

    override suspend fun getStorageUsage(): Flow<StorageUsage> {
        return observePreferences().map {
            val modelsDir = File(context.filesDir, "models")
            val cacheDir = context.cacheDir
            val historyFile = context.getDatabasePath("kurio.db")

            StorageUsage(
                modelsBytes = modelsDir.totalSize(),
                historyBytes = if (historyFile.exists()) historyFile.length() else 0,
                cacheBytes = cacheDir.totalSize()
            )
        }
    }

    override suspend fun clearCache() {
        val cacheDir = context.cacheDir
        cacheDir.deleteRecursively()
        cacheDir.mkdirs()
    }

    override suspend fun setOnboardingCompleted(completed: Boolean) {
        context.dataStore.edit { it[KEY_ONBOARDING_COMPLETED] = completed }
    }

    override suspend fun isOnboardingCompleted(): Boolean {
        return context.dataStore.data.first()[KEY_ONBOARDING_COMPLETED] ?: false
    }

    override suspend fun checkForUpdates(): UpdateCheckResult {
        return try {
            val response = api.checkForUpdate("1.0.0")
            val hasUpdate = response.version != "1.0.0"

            val latestVersion = if (hasUpdate) {
                AppVersion(
                    version = response.version,
                    title = response.title,
                    critical = response.critical,
                    description = response.description,
                    features = response.features,
                    releaseDate = response.releaseDate,
                    downloadUrl = response.downloadUrl
                )
            } else null

            setLastUpdateCheck(System.currentTimeMillis())

            UpdateCheckResult(
                hasUpdate = hasUpdate,
                latestVersion = latestVersion,
                checkTimestamp = System.currentTimeMillis()
            )
        } catch (e: Exception) {
            Napier.e("UpdateCheck", throwable = e) { "Failed to check for updates" }
            UpdateCheckResult(
                hasUpdate = false,
                checkTimestamp = System.currentTimeMillis()
            )
        }
    }

    override suspend fun getDismissedAnnouncements(): Set<String> {
        return context.dataStore.data.first()[KEY_DISMISSED_ANNOUNCEMENTS]
            ?.split(",")
            ?.filter { it.isNotBlank() }
            ?.toSet() ?: emptySet()
    }

    override suspend fun dismissAnnouncement(id: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DISMISSED_ANNOUNCEMENTS] ?: ""
            val updated = if (current.isBlank()) id else "$current,$id"
            prefs[KEY_DISMISSED_ANNOUNCEMENTS] = updated
        }
    }

    override suspend fun getLastUpdateCheck(): Long {
        return context.dataStore.data.first()[KEY_LAST_UPDATE_CHECK] ?: 0L
    }

    override suspend fun setLastUpdateCheck(timestamp: Long) {
        context.dataStore.edit { it[KEY_LAST_UPDATE_CHECK] = timestamp }
    }

    override suspend fun getChangelog(): List<AppVersion> {
        return try {
            api.getChangelog().map { response ->
                AppVersion(
                    version = response.version,
                    title = response.title,
                    critical = response.critical,
                    description = response.description,
                    features = response.features,
                    releaseDate = response.releaseDate,
                    downloadUrl = response.downloadUrl
                )
            }
        } catch (e: Exception) {
            Napier.e("Changelog", throwable = e) { "Failed to fetch changelog" }
            emptyList()
        }
    }

    private fun File.totalSize(): Long {
        if (!exists()) return 0
        if (isFile) return length()
        var size = 0L
        listFiles()?.forEach { size += it.totalSize() }
        return size
    }
}
