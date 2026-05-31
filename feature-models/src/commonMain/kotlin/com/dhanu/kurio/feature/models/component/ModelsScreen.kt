package com.dhanu.kurio.feature.models.component

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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
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

        Spacer(Modifier.height(20.dp))

        if (state.isLoading) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = KurioColors.Accent)
            }
        } else {
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(state.models, key = { it.id }) { model ->
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
                        if (model.isExperimental) {
                            Spacer(Modifier.width(8.dp))
                            Text(
                                text = "EXP",
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = KurioColors.Accent,
                                modifier = Modifier
                                    .background(
                                        KurioColors.Accent.copy(alpha = 0.15f),
                                        RoundedCornerShape(4.dp)
                                    )
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            )
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
                ModelInfo("Speed", "★".repeat(model.speedRating))
                ModelInfo("Accuracy", "★".repeat(model.accuracyRating))
            }

            Spacer(Modifier.height(12.dp))

            when (model.status) {
                ModelStatus.NOT_DOWNLOADED -> {
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
                ModelStatus.DOWNLOADED, ModelStatus.VERIFIED -> {
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
                ModelStatus.VERIFYING -> {
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
