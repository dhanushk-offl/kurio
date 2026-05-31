package com.dhanu.kurio.feature.history.component

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.model.HistoryEntry
import com.dhanu.kurio.core.util.TimeUtils
import com.dhanu.kurio.feature.history.viewmodel.HistoryUiState
import com.dhanu.kurio.presentation.component.textfield.KurioTextField
import com.dhanu.kurio.presentation.component.dialog.KurioDialog

@Composable
fun HistoryScreen(
    state: HistoryUiState,
    onSearchQueryChange: (String) -> Unit,
    onDeleteEntry: (String) -> Unit,
    onClearHistory: () -> Unit,
    onBack: () -> Unit,
    onCopy: (String) -> Unit,
    onShare: (String) -> Unit
) {
    var showClearDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "History",
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = KurioColors.PrimaryText
            )
            if (state.entries.isNotEmpty()) {
                TextButton(onClick = { showClearDialog = true }) {
                    Text("Clear All", color = KurioColors.Error, fontWeight = FontWeight.Medium)
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        KurioTextField(
            value = state.searchQuery,
            onValueChange = onSearchQueryChange,
            placeholder = "Search transcriptions...",
            trailingIcon = {
                if (state.searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChange("") }) {
                        Text("✕", fontSize = 16.sp)
                    }
                }
            }
        )

        Spacer(Modifier.height(16.dp))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = KurioColors.Accent)
            }
        } else if (state.entries.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("📜", fontSize = 48.sp)
                    Spacer(Modifier.height(16.dp))
                    Text(
                        text = "No transcriptions yet",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Medium,
                        color = KurioColors.SecondaryText
                    )
                    Text(
                        text = "Your recordings will appear here",
                        fontSize = 14.sp,
                        color = KurioColors.SecondaryText.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.entries, key = { it.id }) { entry ->
                    HistoryItem(
                        entry = entry,
                        onCopy = { onCopy(entry.text) },
                        onShare = { onShare(entry.text) },
                        onDelete = { onDeleteEntry(entry.id) }
                    )
                }
            }
        }
    }

    if (showClearDialog) {
        KurioDialog(
            title = "Clear History",
            message = "This will permanently delete all transcription history. This action cannot be undone.",
            onDismiss = { showClearDialog = false },
            onConfirm = {
                onClearHistory()
                showClearDialog = false
            },
            confirmText = "Clear",
            isDestructive = true
        )
    }
}

@Composable
private fun HistoryItem(
    entry: HistoryEntry,
    onCopy: () -> Unit,
    onShare: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = KurioColors.Surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = entry.text,
                fontSize = 15.sp,
                lineHeight = 22.sp,
                color = KurioColors.PrimaryText,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TimeUtils.formatTimestamp(entry.dateMillis),
                    fontSize = 12.sp,
                    color = KurioColors.SecondaryText
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = TimeUtils.formatDuration(entry.durationMs),
                    fontSize = 12.sp,
                    color = KurioColors.SecondaryText
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = "${entry.wordCount} words",
                    fontSize = 12.sp,
                    color = KurioColors.SecondaryText
                )
                Spacer(Modifier.weight(1f))
                Row {
                    IconButton(onClick = onCopy, modifier = Modifier.size(32.dp)) {
                        Text("📋", fontSize = 14.sp)
                    }
                    IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
                        Text("📤", fontSize = 14.sp)
                    }
                    IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
                        Text("🗑", fontSize = 14.sp)
                    }
                }
            }
        }
    }
}
