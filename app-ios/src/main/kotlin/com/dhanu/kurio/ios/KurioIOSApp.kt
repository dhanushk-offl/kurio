package com.dhanu.kurio.ios

import androidx.compose.runtime.Composable
import com.dhanu.kurio.core.design.theme.KurioTheme
import com.dhanu.kurio.feature.home.component.HomeScreen
import com.dhanu.kurio.feature.home.viewmodel.HomeViewModel
import org.koin.compose.koinInject

@Composable
fun KurioIOSApp() {
    KurioTheme {
        val viewModel: HomeViewModel = koinInject()
        HomeScreen(
            state = viewModel.uiState.value,
            onNavigateToTranscription = { },
            onNavigateToHistory = { },
            onNavigateToModels = { },
            onNavigateToSettings = { },
            onNavigateToAbout = { }
        )
    }
}
