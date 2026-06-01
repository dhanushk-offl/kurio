package com.dhanu.kurio.feature.settings.viewmodel

import com.dhanu.kurio.core.model.AppPreferences
import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.core.model.StorageUsage
import com.dhanu.kurio.domain.usecase.settings.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelChildren
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val preferences: AppPreferences = AppPreferences(),
    val storageUsage: StorageUsage = StorageUsage(),
    val appVersion: String = "1.0.0",
    val appVersionCode: String = "1",
    val isLoading: Boolean = true
)

class SettingsViewModel(
    private val observePreferences: ObservePreferencesUseCase,
    private val updateAutoCopy: UpdateAutoCopyUseCase,
    private val updateHaptic: UpdateHapticFeedbackUseCase,
    private val updateDarkMode: UpdateDarkModeUseCase,
    private val updateAnalytics: UpdateAnalyticsUseCase,
    private val updateNotifications: UpdateNotificationsUseCase,
    private val updateReleaseChannel: UpdateReleaseChannelUseCase,
    private val getStorageUsage: GetStorageUsageUseCase,
    private val clearCache: ClearCacheUseCase
) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun initialize() {
        scope.launch {
            observePreferences().collect { prefs ->
                _uiState.update { it.copy(preferences = prefs) }
            }
        }
        scope.launch {
            getStorageUsage().collect { usage ->
                _uiState.update { it.copy(storageUsage = usage, isLoading = false) }
            }
        }
    }

    fun onToggleAutoCopy(enabled: Boolean) {
        scope.launch { updateAutoCopy(enabled) }
    }

    fun onToggleHapticFeedback(enabled: Boolean) {
        scope.launch { updateHaptic(enabled) }
    }

    fun onToggleDarkMode(enabled: Boolean) {
        scope.launch { updateDarkMode(enabled) }
    }

    fun onToggleAnalytics(enabled: Boolean) {
        scope.launch { updateAnalytics(enabled) }
    }

    fun onToggleNotifications(enabled: Boolean) {
        scope.launch { updateNotifications(enabled) }
    }

    fun onUpdateReleaseChannel(channel: ReleaseChannel) {
        scope.launch { updateReleaseChannel(channel) }
    }

    fun onClearCache() {
        scope.launch {
            clearCache()
            getStorageUsage().first { usage ->
                _uiState.update { settings -> settings.copy(storageUsage = usage) }
                true
            }
        }
    }

    fun onCleared() {
        scope.coroutineContext.cancelChildren()
    }
}
