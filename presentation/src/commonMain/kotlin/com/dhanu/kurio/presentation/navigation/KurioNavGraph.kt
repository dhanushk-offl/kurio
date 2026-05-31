package com.dhanu.kurio.presentation.navigation

import androidx.compose.runtime.Composable
import com.dhanu.kurio.presentation.theme.KurioTheme
import com.dhanu.kurio.core.design.theme.KurioTheme as CoreKurioTheme

@Composable
fun KurioNavGraph(
    startScreen: Screen = Screen.Home,
    isOnboardingCompleted: Boolean = true
) {
    CoreKurioTheme {
        val start = if (isOnboardingCompleted) Screen.Home else Screen.Onboarding
    }
}
