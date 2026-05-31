package com.dhanu.kurio.feature.transcription.component

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import com.dhanu.kurio.feature.transcription.viewmodel.TranscriptionUiState
import com.dhanu.kurio.core.util.TimeUtils

@Composable
fun TranscriptionScreen(
    state: TranscriptionUiState,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onCancel: () -> Unit,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit,
    onEdit: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = when (state.state) {
                TranscriptionState.IDLE -> "Tap to Record"
                TranscriptionState.LISTENING -> "Listening..."
                TranscriptionState.PROCESSING -> "Transcribing..."
                TranscriptionState.COMPLETED -> "Done"
                TranscriptionState.ERROR -> "Error"
            },
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText
        )

        Spacer(Modifier.height(8.dp))

        if (state.state == TranscriptionState.LISTENING) {
            Text(
                text = formatDuration(state.elapsedSeconds),
                fontSize = 48.sp,
                fontWeight = FontWeight.Bold,
                color = KurioColors.Accent
            )
        }

        Spacer(Modifier.weight(1f))

        RecordButton(
            state = state.state,
            onStart = onStartRecording,
            onStop = onStopRecording
        )

        Spacer(Modifier.height(24.dp))

        if (state.state == TranscriptionState.LISTENING) {
            Text(
                text = "Tap again to stop",
                fontSize = 14.sp,
                color = KurioColors.SecondaryText
            )
        }

        Spacer(Modifier.weight(1f))

        if (state.result != null) {
            TranscriptionResultCard(
                text = state.result.text,
                wordCount = state.result.wordCount,
                charCount = state.result.characterCount,
                duration = TimeUtils.formatDuration(state.result.durationMs),
                onCopy = { onCopy(state.result.text) },
                onShare = { onShare(state.result.text) },
                onEdit = { onEdit(state.result.text) },
                onDelete = onCancel
            )
        }

        if (state.error != null) {
            ErrorCard(message = state.error)
        }

        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun RecordButton(
    state: TranscriptionState,
    onStart: () -> Unit,
    onStop: () -> Unit
) {
    val isActive = state == TranscriptionState.LISTENING
    val containerColor by animateColorAsState(
        targetValue = if (isActive) KurioColors.Error else KurioColors.Accent,
        label = "recordColor"
    )

    Box(
        modifier = Modifier
            .size(88.dp)
            .shadow(
                elevation = if (isActive) 16.dp else 8.dp,
                shape = CircleShape,
                spotColor = containerColor.copy(alpha = if (isActive) 0.4f else 0.2f)
            )
            .clip(CircleShape)
            .background(containerColor)
            .then(
                Modifier
                    .then(
                        Modifier
                            .let { if (isActive) Modifier.fillMaxSize() else Modifier.size(80.dp) }
                    )
            ),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.IconButton(
            onClick = {
                if (isActive) onStop() else onStart()
            }
        ) {
            if (isActive) {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(KurioColors.White)
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(KurioColors.White)
                )
            }
        }
    }
}

@Composable
private fun TranscriptionResultCard(
    text: String,
    wordCount: Int,
    charCount: Int,
    duration: String,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(4.dp, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = KurioColors.Surface)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = text,
                fontSize = 18.sp,
                lineHeight = 28.sp,
                color = KurioColors.PrimaryText
            )

            Spacer(Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                StatLabel("Words", "$wordCount")
                StatLabel("Chars", "$charCount")
                StatLabel("Duration", duration)
            }

            Spacer(Modifier.height(16.dp))
            HorizontalDivider(color = KurioColors.Border.copy(alpha = 0.5f))
            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                ActionChip("Copy", onClick = onCopy)
                ActionChip("Share", onClick = onShare)
                ActionChip("Edit", onClick = onEdit)
                ActionChip("Delete", onClick = onDelete, isDestructive = true)
            }
        }
    }
}

@Composable
private fun StatLabel(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.Accent
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = KurioColors.SecondaryText,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun ActionChip(
    text: String,
    onClick: () -> Unit,
    isDestructive: Boolean = false
) {
    TextButton(onClick = onClick) {
        Text(
            text = text,
            fontSize = 13.sp,
            fontWeight = FontWeight.Medium,
            color = if (isDestructive) KurioColors.Error else KurioColors.Accent
        )
    }
}

@Composable
private fun ErrorCard(message: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = KurioColors.Error.copy(alpha = 0.1f))
    ) {
        Text(
            text = message,
            modifier = Modifier.padding(16.dp),
            color = KurioColors.Error,
            fontSize = 14.sp
        )
    }
}

private fun formatDuration(seconds: Int): String {
    val mins = seconds / 60
    val secs = seconds % 60
    return "${mins.toString().padStart(2, '0')}:${secs.toString().padStart(2, '0')}"
}
