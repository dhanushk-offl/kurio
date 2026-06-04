package com.dhanu.kurio.feature.models.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.spacing.KurioSpacing
import com.dhanu.kurio.core.model.ModelStatus
import com.dhanu.kurio.core.model.SpeechModel
import com.dhanu.kurio.core.util.StorageUtils
import com.dhanu.kurio.feature.models.viewmodel.ModelsUiState
import com.dhanu.kurio.presentation.component.dialog.KurioDialog

@Composable
fun ModelsScreen(
    state: ModelsUiState,
    onDownload: (String) -> Unit,
    onPause: (String) -> Unit,
    onResume: (String) -> Unit,
    onDelete: (String) -> Unit,
    onActivate: (String) -> Unit,
    onBack: () -> Unit
) {
    var showDeleteDialog by remember { mutableStateOf<String?>(null) }
    var query by remember { mutableStateOf("") }
    var filter by remember { mutableStateOf(ModelFilter.All) }
    val visibleModels = remember(state.models, query, filter) {
        state.models.filter { model ->
            val matchesQuery = query.isBlank() ||
                model.name.contains(query, ignoreCase = true) ||
                model.language.contains(query, ignoreCase = true) ||
                model.provider.contains(query, ignoreCase = true)
            val matchesFilter = when (filter) {
                ModelFilter.All -> true
                ModelFilter.Recommended -> model.id == RecommendedModelId
                ModelFilter.Installed -> model.status in InstalledStatuses
                ModelFilter.Experimental -> model.isExperimental
            }
            matchesQuery && matchesFilter
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .padding(horizontal = 20.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "Speech Models",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = "Download and manage transcription models",
            fontSize = 14.sp,
            color = KurioColors.SecondaryText
        )

        Spacer(Modifier.height(KurioSpacing.Xl))

        OutlinedTextField(
            value = query,
            onValueChange = { query = it },
            modifier = Modifier.fillMaxWidth(),
            shape = MaterialTheme.shapes.medium,
            singleLine = true,
            placeholder = { Text("Search models, languages, providers") }
        )

        Spacer(Modifier.height(KurioSpacing.Md))

        Row(horizontalArrangement = Arrangement.spacedBy(KurioSpacing.Sm)) {
            ModelFilter.entries.forEach { item ->
                FilterChip(
                    selected = filter == item,
                    onClick = { filter = item },
                    label = { Text(item.label) }
                )
            }
        }

        Spacer(Modifier.height(KurioSpacing.Xl))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KurioColors.Accent)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(visibleModels, key = { it.id }) { model ->
                    ModelCard(
                        model = model,
                        progress = state.downloadProgress[model.id] ?: 0f,
                        isActive = model.id == state.activeModelId,
                        onDownload = { onDownload(model.id) },
                        onPause = { onPause(model.id) },
                        onResume = { onResume(model.id) },
                        onDelete = { showDeleteDialog = model.id },
                        onActivate = { onActivate(model.id) }
                    )
                }
            }
        }
    }

    showDeleteDialog?.let { modelId ->
        val model = state.models.find { it.id == modelId }
        KurioDialog(
            title = "Delete Model",
            message = "Are you sure you want to delete ${model?.name ?: "this model"}? This will free up storage space.",
            onDismiss = { showDeleteDialog = null },
            onConfirm = {
                onDelete(modelId)
                showDeleteDialog = null
            },
            confirmText = "Delete",
            isDestructive = true
        )
    }
}

@Composable
private fun ModelCard(
    model: SpeechModel,
    progress: Float,
    isActive: Boolean,
    onDownload: () -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onDelete: () -> Unit,
    onActivate: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(2.dp, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) KurioColors.Accent.copy(alpha = 0.08f)
            else KurioColors.Surface
        )
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = model.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = KurioColors.PrimaryText
                        )
                        if (model.id == RecommendedModelId) {
                            Spacer(Modifier.width(8.dp))
                            BadgeLabel("Recommended")
                        }
                        if (model.isExperimental) {
                            Spacer(Modifier.width(8.dp))
                            BadgeLabel("Experimental")
                        }
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                        text = model.description,
                        fontSize = 13.sp,
                        color = KurioColors.SecondaryText,
                        lineHeight = 18.sp
                    )
                }
            }

            Spacer(Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                ModelInfo("Size", StorageUtils.formatBytes(model.sizeBytes))
                ModelInfo("RAM", "${model.ramUsageMb} MB")
                ModelInfo("Performance", performanceLabel(model.speedRating))
                ModelInfo("Language", model.language)
            }

            Spacer(Modifier.height(12.dp))

            when (model.status) {
                ModelStatus.NOT_INSTALLED, ModelStatus.DELETED, ModelStatus.UNLOADED -> {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KurioColors.Accent,
                            contentColor = KurioColors.White
                        )
                    ) {
                        Text("Download", fontWeight = FontWeight.Medium)
                    }
                }
                ModelStatus.DOWNLOADING -> {
                    Column {
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .shadow(1.dp, RoundedCornerShape(3.dp)),
                            color = KurioColors.Accent,
                            trackColor = KurioColors.Border.copy(alpha = 0.3f)
                        )
                        Spacer(Modifier.height(8.dp))
                        Button(
                            onClick = onPause,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KurioColors.SecondarySurface,
                                contentColor = KurioColors.PrimaryText
                            )
                        ) {
                            Text("Pause", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                ModelStatus.PAUSED -> {
                    Button(
                        onClick = onResume,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KurioColors.Accent,
                            contentColor = KurioColors.White
                        )
                    ) {
                        Text("Resume", fontWeight = FontWeight.Medium)
                    }
                }
                ModelStatus.DOWNLOADED, ModelStatus.INSTALLED, ModelStatus.IDLE, ModelStatus.WARM -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KurioColors.Error)
                        ) {
                            Text("Delete", fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = onActivate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KurioColors.Accent,
                                contentColor = KurioColors.White
                            )
                        ) {
                            Text("Activate", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                ModelStatus.ACTIVE -> {
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        OutlinedButton(
                            onClick = onDelete,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = KurioColors.Error)
                        ) {
                            Text("Delete", fontWeight = FontWeight.Medium)
                        }
                        Button(
                            onClick = onActivate,
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            enabled = false,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = KurioColors.Success,
                                contentColor = KurioColors.White
                            )
                        ) {
                            Text("Active", fontWeight = FontWeight.Medium)
                        }
                    }
                }
                ModelStatus.CORRUPTED, ModelStatus.ERROR -> {
                    Button(
                        onClick = onDownload,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = KurioColors.Error,
                            contentColor = KurioColors.White
                        )
                    ) {
                        Text("Retry Download", fontWeight = FontWeight.Medium)
                    }
                }
                ModelStatus.VERIFYING, ModelStatus.LOADING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Center,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(16.dp),
                            color = KurioColors.Accent,
                            strokeWidth = 2.dp
                        )
                        Spacer(Modifier.width(8.dp))
                        Text("Verifying...", fontSize = 13.sp, color = KurioColors.SecondaryText)
                    }
                }
            }
        }
    }
}

@Composable
private fun ModelInfo(label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = KurioColors.Accent
        )
        Text(
            text = label,
            fontSize = 10.sp,
            color = KurioColors.SecondaryText,
            letterSpacing = 0.5.sp
        )
    }
}

@Composable
private fun BadgeLabel(text: String) {
    Text(
        text = text,
        fontSize = 10.sp,
        fontWeight = FontWeight.Bold,
        color = KurioColors.Accent,
        modifier = Modifier
            .background(
                KurioColors.Accent.copy(alpha = 0.12f),
                RoundedCornerShape(4.dp)
            )
            .padding(horizontal = 6.dp, vertical = 2.dp)
    )
}

private fun performanceLabel(speedRating: Int): String = when (speedRating) {
    5 -> "Very fast"
    4 -> "Fast"
    3 -> "Balanced"
    2 -> "Accurate"
    else -> "Heavy"
}

private enum class ModelFilter(val label: String) {
    All("All"),
    Recommended("Recommended"),
    Installed("Installed"),
    Experimental("Experimental")
}

private val InstalledStatuses = setOf(
    ModelStatus.DOWNLOADED,
    ModelStatus.INSTALLED,
    ModelStatus.LOADING,
    ModelStatus.WARM,
    ModelStatus.ACTIVE,
    ModelStatus.IDLE
)

private const val RecommendedModelId = "whisper-tiny-en"
