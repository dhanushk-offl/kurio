package com.dhanu.kurio

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.spacing.KurioSpacing
import com.dhanu.kurio.core.design.theme.KurioTheme
import com.dhanu.kurio.core.model.StorageUsage
import com.dhanu.kurio.feature.history.component.HistoryScreen
import com.dhanu.kurio.feature.history.viewmodel.HistoryViewModel
import com.dhanu.kurio.feature.home.component.HomeScreen
import com.dhanu.kurio.feature.home.viewmodel.HomeViewModel
import com.dhanu.kurio.feature.models.component.ModelsScreen
import com.dhanu.kurio.feature.models.viewmodel.ModelsViewModel
import com.dhanu.kurio.feature.settings.component.SettingsScreen
import com.dhanu.kurio.feature.settings.viewmodel.SettingsViewModel
import com.dhanu.kurio.feature.transcription.component.TranscriptionScreen
import com.dhanu.kurio.feature.transcription.viewmodel.TranscriptionViewModel
import com.dhanu.kurio.presentation.component.card.KurioCard
import com.dhanu.kurio.presentation.component.overlay.OnboardingScreen
import com.dhanu.kurio.presentation.component.screen.KurioScreen
import com.dhanu.kurio.presentation.component.splash.AnimatedSplashScreen
import com.dhanu.kurio.presentation.navigation.Screen
import org.koin.compose.koinInject

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
fun KurioApp(
    navController: NavHostController = rememberNavController(),
    isOnboardingCompleted: Boolean = true
) {
    val startRoute = if (isOnboardingCompleted) Screen.Home.route else Screen.Onboarding.route

    NavHost(
        navController = navController,
        startDestination = startRoute
    ) {
        composable(Screen.Onboarding.route) {
            OnboardingScreen(
                onComplete = {
                    navController.navigate(Screen.Home.route) {
                        popUpTo(Screen.Onboarding.route) { inclusive = true }
                    }
                }
            )
        }

        composable(Screen.Home.route) {
            val viewModel: HomeViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            HomeScreen(
                state = state,
                onNavigateToTranscription = { navController.navigateSingleTop(Screen.Transcription) },
                onNavigateToHistory = { navController.navigateSingleTop(Screen.History) },
                onNavigateToModels = { navController.navigateSingleTop(Screen.Models) },
                onNavigateToSettings = { navController.navigateSingleTop(Screen.Settings) },
                onNavigateToAbout = { navController.navigateSingleTop(Screen.About) }
            )
        }

        composable(Screen.Transcription.route) {
            val viewModel: TranscriptionViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            TranscriptionScreen(
                state = state,
                onStartRecording = { viewModel.onStartRecording() },
                onStopRecording = { viewModel.onStopRecording() },
                onCancel = { viewModel.onCancel() },
                onBack = { navController.popBackStack() },
                onCopy = { },
                onShare = { },
                onEdit = { }
            )
        }

        composable(Screen.History.route) {
            val viewModel: HistoryViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()

            HistoryScreen(
                state = state,
                onSearchQueryChange = { viewModel.onSearchQueryChange(it) },
                onDeleteEntry = { viewModel.onDeleteEntry(it) },
                onClearHistory = { viewModel.onClearHistory() },
                onBack = { navController.popBackStack() },
                onCopy = { },
                onShare = { }
            )
        }

        composable(Screen.Models.route) {
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
                onBack = { navController.popBackStack() }
            )
        }

        composable(Screen.Settings.route) {
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
                onBack = { navController.popBackStack() },
                onNavigateToAbout = { navController.navigateSingleTop(Screen.About) },
                onNavigateToChangelog = { navController.navigateSingleTop(Screen.Updates) }
            )
        }

        composable(Screen.Storage.route) {
            val viewModel: SettingsViewModel = koinInject()
            LaunchedEffect(Unit) { viewModel.initialize() }
            val state by viewModel.uiState.collectAsState()
            StorageScreen(storageUsage = state.storageUsage)
        }

        composable(Screen.Updates.route) {
            UpdatesScreen()
        }

        composable(Screen.About.route) {
            AboutScreen()
        }
    }
}

private fun NavHostController.navigateSingleTop(screen: Screen) {
    navigate(screen.route) {
        launchSingleTop = true
        restoreState = true
    }
}

@Composable
private fun StorageScreen(storageUsage: StorageUsage) {
    KurioScreen(
        title = "Storage",
        subtitle = "Model binaries, cache, and transcription history."
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(KurioSpacing.Md)) {
            StorageRow("Models", storageUsage.formattedModels)
            StorageRow("History", storageUsage.formattedHistory)
            StorageRow("Cache", storageUsage.formattedCache)
            StorageRow("Total", storageUsage.formattedTotal, prominent = true)
        }
    }
}

@Composable
private fun StorageRow(label: String, value: String, prominent: Boolean = false) {
    Card(
        colors = CardDefaults.cardColors(containerColor = KurioColors.Surface),
        shape = MaterialTheme.shapes.large,
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(KurioSpacing.Xl),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge,
                color = KurioColors.PrimaryText,
                fontWeight = if (prominent) FontWeight.SemiBold else FontWeight.Normal
            )
            Text(
                text = value,
                style = MaterialTheme.typography.bodyLarge,
                color = KurioColors.SecondaryText,
                fontWeight = if (prominent) FontWeight.SemiBold else FontWeight.Normal
            )
        }
    }
}

@Composable
private fun UpdatesScreen() {
    KurioScreen(
        title = "Updates",
        subtitle = "Release channel, update checks, and version history."
    ) {
        KurioCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Kurio 1.0.0",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = KurioColors.PrimaryText
            )
            Spacer(Modifier.height(KurioSpacing.Sm))
            Text(
                text = "Initial Android production shell with local-first transcription, model lifecycle management, overlay services, and private on-device history.",
                style = MaterialTheme.typography.bodyMedium,
                color = KurioColors.SecondaryText
            )
        }
    }
}

@Composable
private fun AboutScreen() {
    KurioScreen(
        title = "About",
        subtitle = "Private speech transcription built for speed."
    ) {
        KurioCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Kurio",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = KurioColors.PrimaryText
            )
            Spacer(Modifier.height(KurioSpacing.Sm))
            Text(
                text = "Version 1.0.0",
                style = MaterialTheme.typography.bodyLarge,
                color = KurioColors.PrimaryText
            )
            Spacer(Modifier.height(KurioSpacing.Xxl))
            Text(
                text = "All transcription is designed to run locally. Speech models are stored as binary inference assets and are never serialized into app settings or history storage.",
                style = MaterialTheme.typography.bodyMedium,
                color = KurioColors.SecondaryText
            )
        }
    }
}
