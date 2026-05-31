package com.dhanu.kurio.feature.home.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.model.TranscriptionState
import com.dhanu.kurio.core.util.StorageUtils
import com.dhanu.kurio.feature.home.viewmodel.HomeUiState
import com.dhanu.kurio.presentation.component.card.KurioStatCard

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
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(60.dp))

        HeaderSection(state = state)

        Spacer(Modifier.height(32.dp))

        QuickActionsSection(
            state = state,
            onTranscribe = onNavigateToTranscription,
            onHistory = onNavigateToHistory,
            onModels = onNavigateToModels
        )

        Spacer(Modifier.height(24.dp))

        StorageSection(state = state)

        Spacer(Modifier.height(24.dp))

        BottomActionsSection(
            onSettings = onNavigateToSettings,
            onAbout = onNavigateToAbout
        )

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun HeaderSection(state: HomeUiState) {
    Column {
        Text(
            text = "Kurio",
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText,
            letterSpacing = (-0.5).sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Speak. Transcribe. Continue.",
            fontSize = 16.sp,
            color = KurioColors.SecondaryText,
            letterSpacing = 0.3.sp
        )
        Spacer(Modifier.height(24.dp))

        if (state.hasUpdate) {
            UpdateBanner(version = state.updateVersion ?: "")
            Spacer(Modifier.height(16.dp))
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            KurioStatCard(
                modifier = Modifier.weight(1f),
                label = "ACTIVE MODEL",
                value = state.activeModel?.name ?: "None"
            )
            KurioStatCard(
                modifier = Modifier.weight(1f),
                label = "HISTORY",
                value = "${state.historyCount}/10"
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
    Column {
        Text(
            text = "Quick Actions",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = KurioColors.SecondaryText,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(12.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            ActionButton(
                modifier = Modifier.weight(1f),
                emoji = "🎤",
                title = "Transcribe",
                subtitle = if (state.transcriptionState == TranscriptionState.LISTENING) "Recording..." else "Start",
                onClick = onTranscribe,
                isActive = state.transcriptionState == TranscriptionState.LISTENING
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                emoji = "📜",
                title = "History",
                subtitle = "${state.historyCount} entries",
                onClick = onHistory
            )
            ActionButton(
                modifier = Modifier.weight(1f),
                emoji = "🧠",
                title = "Models",
                subtitle = state.activeModel?.name?.take(12) ?: "Download",
                onClick = onModels
            )
        }
    }
}

@Composable
private fun ActionButton(
    modifier: Modifier = Modifier,
    emoji: String,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    isActive: Boolean = false
) {
    Column(
        modifier = modifier
            .shadow(
                elevation = if (isActive) 8.dp else 2.dp,
                shape = RoundedCornerShape(16.dp),
                spotColor = if (isActive) KurioColors.Accent.copy(alpha = 0.2f)
                else KurioColors.Accent.copy(alpha = 0.05f)
            )
            .clip(RoundedCornerShape(16.dp))
            .background(if (isActive) KurioColors.Accent.copy(alpha = 0.1f) else KurioColors.Surface)
            .clickable(onClick = onClick)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = emoji, fontSize = 28.sp)
        Spacer(Modifier.height(8.dp))
        Text(
            text = title,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = KurioColors.PrimaryText
        )
        Spacer(Modifier.height(2.dp))
        Text(
            text = subtitle,
            fontSize = 10.sp,
            color = KurioColors.SecondaryText
        )
    }
}

@Composable
private fun StorageSection(state: HomeUiState) {
    KurioStatCard(
        modifier = Modifier.fillMaxWidth(),
        label = "STORAGE",
        value = state.storageUsage.formattedTotal
    )
}

@Composable
private fun BottomActionsSection(
    onSettings: () -> Unit,
    onAbout: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedButton(
            onClick = onSettings,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = KurioColors.Accent),
            border = androidx.compose.foundation.BorderStroke(1.dp, KurioColors.Border)
        ) {
            Text("Settings", fontWeight = FontWeight.Medium)
        }
        OutlinedButton(
            onClick = onAbout,
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = KurioColors.Accent),
            border = androidx.compose.foundation.BorderStroke(1.dp, KurioColors.Border)
        ) {
            Text("About", fontWeight = FontWeight.Medium)
        }
    }
}

@Composable
private fun UpdateBanner(version: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(KurioColors.Accent.copy(alpha = 0.1f))
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "Update Available",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = KurioColors.Accent
            )
            Text(
                text = "Version $version is ready",
                fontSize = 12.sp,
                color = KurioColors.SecondaryText
            )
        }
        Text(
            text = "View",
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = KurioColors.Accent
        )
    }
}
