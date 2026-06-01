package com.dhanu.kurio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.theme.KurioTheme
import com.dhanu.kurio.feature.home.component.HomeScreen
import com.dhanu.kurio.feature.home.viewmodel.HomeViewModel
import com.dhanu.kurio.feature.history.component.HistoryScreen
import com.dhanu.kurio.feature.history.viewmodel.HistoryViewModel
import com.dhanu.kurio.feature.models.component.ModelsScreen
import com.dhanu.kurio.feature.models.viewmodel.ModelsViewModel
import com.dhanu.kurio.feature.settings.component.SettingsScreen
import com.dhanu.kurio.feature.settings.viewmodel.SettingsViewModel
import com.dhanu.kurio.feature.transcription.component.TranscriptionScreen
import com.dhanu.kurio.feature.transcription.viewmodel.TranscriptionViewModel
import com.dhanu.kurio.presentation.component.splash.AnimatedSplashScreen
import org.koin.compose.koinInject

enum class AppScreen {
    HOME, TRANSCRIPTION, HISTORY, MODELS, SETTINGS, ABOUT
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KurioTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = KurioColors.Background
                ) {
                    var showSplash by remember { mutableStateOf(true) }
                    if (showSplash) {
                        AnimatedSplashScreen(onSplashComplete = { showSplash = false })
                    } else {
                        KurioApp()
                    }
                }
            }
        }
    }
}

@Composable
fun KurioApp() {
    var currentScreen by remember { mutableStateOf(AppScreen.HOME) }

    when (currentScreen) {
        AppScreen.HOME -> {
            val viewModel: HomeViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            HomeScreen(
                state = state,
                onNavigateToTranscription = { currentScreen = AppScreen.TRANSCRIPTION },
                onNavigateToHistory = { currentScreen = AppScreen.HISTORY },
                onNavigateToModels = { currentScreen = AppScreen.MODELS },
                onNavigateToSettings = { currentScreen = AppScreen.SETTINGS },
                onNavigateToAbout = { currentScreen = AppScreen.ABOUT }
            )
        }

        AppScreen.TRANSCRIPTION -> {
            val viewModel: TranscriptionViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            TranscriptionScreen(
                state = state,
                onStartRecording = { viewModel.onStartRecording() },
                onStopRecording = { viewModel.onStopRecording() },
                onCancel = { viewModel.onCancel() },
                onBack = { currentScreen = AppScreen.HOME },
                onCopy = { },
                onShare = { },
                onEdit = { }
            )
        }

        AppScreen.HISTORY -> {
            val viewModel: HistoryViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            HistoryScreen(
                state = state,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onDeleteEntry = { viewModel.onDeleteEntry(it) },
                onClearHistory = { viewModel.onClearHistory() },
                onBack = { currentScreen = AppScreen.HOME },
                onCopy = { },
                onShare = { }
            )
        }

        AppScreen.MODELS -> {
            val viewModel: ModelsViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            ModelsScreen(
                state = state,
                onDownload = { viewModel.onDownloadModel(it) },
                onPause = { viewModel.onPauseDownload(it) },
                onResume = { viewModel.onResumeDownload(it) },
                onDelete = { viewModel.onDeleteModel(it) },
                onActivate = { viewModel.onActivateModel(it) },
                onBack = { currentScreen = AppScreen.HOME }
            )
        }

        AppScreen.SETTINGS -> {
            val viewModel: SettingsViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            SettingsScreen(
                state = state,
                onToggleAutoCopy = { viewModel.onToggleAutoCopy(it) },
                onToggleHapticFeedback = { viewModel.onToggleHapticFeedback(it) },
                onToggleDarkMode = { viewModel.onToggleDarkMode(it) },
                onToggleAnalytics = { viewModel.onToggleAnalytics(it) },
                onToggleNotifications = { viewModel.onToggleNotifications(it) },
                onUpdateReleaseChannel = { viewModel.onUpdateReleaseChannel(it) },
                onClearCache = { viewModel.onClearCache() },
                onBack = { currentScreen = AppScreen.HOME },
                onNavigateToAbout = { currentScreen = AppScreen.ABOUT },
                onNavigateToChangelog = { }
            )
        }

        AppScreen.ABOUT -> {
            AboutScreen(onBack = { currentScreen = AppScreen.HOME })
        }
    }
}

@Composable
private fun AboutScreen(onBack: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .padding(20.dp)
    ) {
        Spacer(Modifier.height(48.dp))

        Text(
            text = "About",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText
        )

        Spacer(Modifier.height(24.dp))

        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KurioColors.Surface),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Kurio",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.Bold,
                    color = KurioColors.PrimaryText
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = "Speak. Transcribe. Continue.",
                    fontSize = 14.sp,
                    color = KurioColors.SecondaryText
                )
                Spacer(Modifier.height(16.dp))
                Text(
                    text = "Version 1.0.0",
                    fontSize = 15.sp,
                    color = KurioColors.PrimaryText
                )
                Spacer(Modifier.height(24.dp))
                Text(
                    text = "Powered by Whisper.cpp, Moonshine, and Vosk.\nAll transcription runs locally on your device.\nNo data ever leaves your phone.",
                    fontSize = 13.sp,
                    color = KurioColors.SecondaryText,
                    lineHeight = 20.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(Modifier.weight(1f))

        Text(
            text = "© 2026 Dhanush. All rights reserved.",
            fontSize = 12.sp,
            color = KurioColors.SecondaryText,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(Modifier.height(32.dp))
    }
}
