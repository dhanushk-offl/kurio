package com.dhanu.kurio.feature.settings.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.model.ReleaseChannel
import com.dhanu.kurio.core.util.StorageUtils
import com.dhanu.kurio.feature.settings.viewmodel.SettingsUiState
import com.dhanu.kurio.presentation.component.dialog.KurioDialog

@Composable
fun SettingsScreen(
    state: SettingsUiState,
    onToggleAutoCopy: (Boolean) -> Unit,
    onToggleHapticFeedback: (Boolean) -> Unit,
    onToggleDarkMode: (Boolean) -> Unit,
    onToggleAnalytics: (Boolean) -> Unit,
    onToggleNotifications: (Boolean) -> Unit,
    onUpdateReleaseChannel: (ReleaseChannel) -> Unit,
    onClearCache: () -> Unit,
    onBack: () -> Unit,
    onNavigateToAbout: () -> Unit,
    onNavigateToChangelog: () -> Unit
) {
    var showChannelDialog by remember { mutableStateOf(false) }
    var showClearCacheDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "Settings",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText,
            modifier = Modifier.padding(horizontal = 20.dp)
        )

        Spacer(Modifier.height(24.dp))

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SettingsSection("GENERAL") {
                SettingsToggle(
                    title = "Auto Copy",
                    subtitle = "Automatically copy transcription to clipboard",
                    checked = state.preferences.autoCopyEnabled,
                    onCheckedChange = onToggleAutoCopy
                )
                SettingsToggle(
                    title = "Haptic Feedback",
                    subtitle = "Vibrate on recording start and stop",
                    checked = state.preferences.hapticFeedbackEnabled,
                    onCheckedChange = onToggleHapticFeedback
                )
                SettingsToggle(
                    title = "Dark Mode",
                    subtitle = "Use dark theme throughout the app",
                    checked = state.preferences.darkModeEnabled,
                    onCheckedChange = onToggleDarkMode
                )
            }

            SettingsSection("PRIVACY") {
                SettingsToggle(
                    title = "Analytics",
                    subtitle = "Help improve Kurio with anonymous usage data",
                    checked = state.preferences.analyticsEnabled,
                    onCheckedChange = onToggleAnalytics
                )
                SettingsToggle(
                    title = "Notifications",
                    subtitle = "Get notified about updates and new models",
                    checked = state.preferences.notificationsEnabled,
                    onCheckedChange = onToggleNotifications
                )
            }

            SettingsSection("UPDATES") {
                SettingsClickable(
                    title = "Release Channel",
                    subtitle = state.preferences.releaseChannel.name,
                    onClick = { showChannelDialog = true }
                )
                SettingsClickable(
                    title = "What's New",
                    subtitle = "View changelog and version history",
                    onClick = onNavigateToChangelog
                )
            }

            SettingsSection("STORAGE") {
                SettingsInfo(
                    title = "Models",
                    value = state.storageUsage.formattedModels
                )
                SettingsInfo(
                    title = "History",
                    value = state.storageUsage.formattedHistory
                )
                SettingsInfo(
                    title = "Cache",
                    value = state.storageUsage.formattedCache
                )
                HorizontalDivider(
                    color = KurioColors.Border.copy(alpha = 0.3f),
                    modifier = Modifier.padding(vertical = 4.dp)
                )
                SettingsInfo(
                    title = "Total",
                    value = state.storageUsage.formattedTotal
                )
                Spacer(Modifier.height(8.dp))
                SettingsClickable(
                    title = "Clear Cache",
                    subtitle = "Free up storage space",
                    onClick = { showClearCacheDialog = true }
                )
            }

            SettingsSection("INFO") {
                SettingsClickable(
                    title = "About",
                    subtitle = "Version ${state.appVersion}",
                    onClick = onNavigateToAbout
                )
            }

            Spacer(Modifier.height(32.dp))
        }
    }

    if (showChannelDialog) {
        KurioDialog(
            title = "Release Channel",
            message = "Choose your update channel:",
            onDismiss = { showChannelDialog = false },
            onConfirm = {
                showChannelDialog = false
            },
            confirmText = "Done"
        )
    }

    if (showClearCacheDialog) {
        KurioDialog(
            title = "Clear Cache",
            message = "This will remove temporary files. Your models and history will not be affected.",
            onDismiss = { showClearCacheDialog = false },
            onConfirm = {
                onClearCache()
                showClearCacheDialog = false
            },
            confirmText = "Clear"
        )
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable ColumnScope.() -> Unit
) {
    Column {
        Spacer(Modifier.height(16.dp))
        Text(
            text = title,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.Accent,
            letterSpacing = 1.sp
        )
        Spacer(Modifier.height(8.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KurioColors.Surface),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                content = content
            )
        }
    }
}

@Composable
private fun SettingsToggle(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = KurioColors.PrimaryText
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = KurioColors.SecondaryText
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = KurioColors.White,
                checkedTrackColor = KurioColors.Accent,
                uncheckedThumbColor = KurioColors.White,
                uncheckedTrackColor = KurioColors.Border
            )
        )
    }
}

@Composable
private fun SettingsClickable(
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = KurioColors.PrimaryText
            )
            subtitle.let {
                Text(
                    text = subtitle,
                    fontSize = 12.sp,
                    color = KurioColors.SecondaryText
                )
            }
        }
        Text(
            text = "›",
            fontSize = 20.sp,
            color = KurioColors.SecondaryText
        )
    }
}

@Composable
private fun SettingsInfo(
    title: String,
    value: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = title,
            fontSize = 15.sp,
            fontWeight = FontWeight.Medium,
            color = KurioColors.PrimaryText
        )
        Text(
            text = value,
            fontSize = 14.sp,
            color = KurioColors.SecondaryText
        )
    }
}
