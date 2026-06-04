package com.dhanu.kurio.feature.home.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.spacing.KurioSpacing
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.feature.home.viewmodel.HomeUiState
import com.dhanu.kurio.presentation.component.card.KurioCard

@Composable
fun HomeScreen(
    state: HomeUiState,
    onNavigateToTranscription: () -> Unit,
    onNavigateToHistory: () -> Unit,
    onNavigateToModels: () -> Unit,
    onNavigateToSettings: () -> Unit,
    onNavigateToAbout: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = KurioSpacing.ScreenHorizontal),
        verticalArrangement = Arrangement.spacedBy(KurioSpacing.Xl)
    ) {
        Spacer(Modifier.height(KurioSpacing.ScreenTop))

        ReadinessHeader(state)

        QuickActionsSection(
            state = state,
            onTranscribe = onNavigateToTranscription,
            onHistory = onNavigateToHistory,
            onModels = onNavigateToModels
        )

        StatusGrid(state)

        RecentActivitySection(state)

        Row(horizontalArrangement = Arrangement.spacedBy(KurioSpacing.Md)) {
            OutlinedButton(
                onClick = onNavigateToSettings,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(KurioSpacing.None, KurioColors.Border)
            ) {
                Text("Settings")
            }
            OutlinedButton(
                onClick = onNavigateToAbout,
                modifier = Modifier.weight(1f),
                shape = MaterialTheme.shapes.medium,
                border = BorderStroke(KurioSpacing.None, KurioColors.Border)
            ) {
                Text("About")
            }
        }

        Spacer(Modifier.height(KurioSpacing.Xxl))
    }
}

@Composable
private fun ReadinessHeader(state: HomeUiState) {
    KurioCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Kurio is ready.",
                    style = MaterialTheme.typography.displaySmall,
                    fontWeight = FontWeight.Bold,
                    color = KurioColors.PrimaryText
                )
                Spacer(Modifier.height(KurioSpacing.Sm))
                Text(
                    text = state.activeModel?.name ?: "Choose a local speech model to begin.",
                    style = MaterialTheme.typography.bodyLarge,
                    color = KurioColors.SecondaryText
                )
            }
            Spacer(Modifier.width(KurioSpacing.Md))
            StatusPill("Ready", KurioColors.Success)
        }
        if (state.hasUpdate) {
            Spacer(Modifier.height(KurioSpacing.Lg))
            Text(
                text = "Update ${state.updateVersion.orEmpty()} is available",
                style = MaterialTheme.typography.bodyMedium,
                color = KurioColors.Accent,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun QuickActionsSection(
    state: HomeUiState,
    onTranscribe: () -> Unit,
    onHistory: () -> Unit,
    onModels: () -> Unit
) {
    SectionTitle("Quick Actions")
    Row(horizontalArrangement = Arrangement.spacedBy(KurioSpacing.Md)) {
        ActionTile(
            title = if (state.transcriptionState == TranscriptionState.LISTENING) "Recording" else "Record",
            label = if (state.transcriptionState == TranscriptionState.LISTENING) "Tap to manage" else "Start dictation",
            onClick = onTranscribe,
            modifier = Modifier.weight(1f),
            isPrimary = true
        )
        ActionTile(
            title = "History",
            label = "${state.historyCount} saved",
            onClick = onHistory,
            modifier = Modifier.weight(1f)
        )
        ActionTile(
            title = "Models",
            label = state.activeModel?.name ?: "Manage",
            onClick = onModels,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun ActionTile(
    title: String,
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPrimary: Boolean = false
) {
    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.large)
            .background(if (isPrimary) KurioColors.Primary else KurioColors.Surface)
            .clickable(onClick = onClick)
            .padding(KurioSpacing.Xl)
    ) {
        Column {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = if (isPrimary) KurioColors.OnPrimary else KurioColors.PrimaryText
            )
            Spacer(Modifier.height(KurioSpacing.Xs))
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = if (isPrimary) KurioColors.OnPrimary.copy(alpha = 0.76f) else KurioColors.SecondaryText
            )
        }
    }
}

@Composable
private fun StatusGrid(state: HomeUiState) {
    SectionTitle("System Status")
    Column(verticalArrangement = Arrangement.spacedBy(KurioSpacing.Md)) {
        StatusRow("Current Active Model", state.activeModel?.name ?: "Not selected", state.activeModel != null)
        StatusRow("Accessibility Status", "Ready for text injection", true)
        StatusRow("Overlay Status", "Available", true)
        StatusRow("Storage Usage", state.storageUsage.formattedTotal, true)
    }
}

@Composable
private fun StatusRow(label: String, value: String, isHealthy: Boolean) {
    KurioCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            StatusDot(if (isHealthy) KurioColors.Success else KurioColors.Warning)
            Spacer(Modifier.width(KurioSpacing.Md))
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium,
                color = KurioColors.SecondaryText,
                modifier = Modifier.weight(1f)
            )
            Spacer(Modifier.width(KurioSpacing.Md))
            Text(
                text = value,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = KurioColors.PrimaryText
            )
        }
    }
}

@Composable
private fun StatusPill(text: String, color: androidx.compose.ui.graphics.Color) {
    Row(
        modifier = Modifier
            .clip(MaterialTheme.shapes.medium)
            .background(color.copy(alpha = 0.12f))
            .padding(horizontal = KurioSpacing.Md, vertical = KurioSpacing.Sm),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StatusDot(color)
        Spacer(Modifier.width(KurioSpacing.Sm))
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = color
        )
    }
}

@Composable
private fun StatusDot(color: androidx.compose.ui.graphics.Color) {
    Box(
        modifier = Modifier
            .clip(CircleShape)
            .background(color)
            .width(KurioSpacing.Sm)
            .height(KurioSpacing.Sm)
    )
}

@Composable
private fun RecentActivitySection(state: HomeUiState) {
    SectionTitle("Recent Activity")
    KurioCard(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = if (state.historyCount == 0) {
                "No recent transcriptions."
            } else {
                "${state.historyCount} transcriptions saved locally."
            },
            style = MaterialTheme.typography.bodyMedium,
            color = KurioColors.SecondaryText
        )
    }

    SectionTitle("Recent Transcriptions")
    Button(
        onClick = {},
        modifier = Modifier.fillMaxWidth(),
        enabled = false,
        shape = MaterialTheme.shapes.medium,
        colors = ButtonDefaults.buttonColors(
            disabledContainerColor = KurioColors.Surface,
            disabledContentColor = KurioColors.SecondaryText
        )
    ) {
        Text("Open History to review, copy, share, or delete entries")
    }
}

@Composable
private fun SectionTitle(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelLarge,
        fontWeight = FontWeight.SemiBold,
        color = KurioColors.SecondaryText
    )
}
