package com.dhanu.kurio.presentation.navigation

sealed class Screen(val route: String) {
    data object Onboarding : Screen("onboarding")
    data object Home : Screen("home")
    data object Transcription : Screen("transcription")
    data object History : Screen("history")
    data object Models : Screen("models")
    data object Settings : Screen("settings")
    data object About : Screen("about")
    data object Changelog : Screen("changelog")
}
