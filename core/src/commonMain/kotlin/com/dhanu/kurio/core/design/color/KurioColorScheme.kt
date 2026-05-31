package com.dhanu.kurio.core.design.color

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class KurioColorScheme(
    val background: Color,
    val surface: Color,
    val secondarySurface: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val success: Color,
    val error: Color,
    val border: Color
)

@Composable
fun kurioColorScheme(): KurioColorScheme {
    return if (isSystemInDarkTheme()) {
        KurioColorScheme(
            background = KurioColors.DarkBackground,
            surface = KurioColors.DarkSurface,
            secondarySurface = KurioColors.DarkSecondarySurface,
            primaryText = KurioColors.DarkPrimaryText,
            secondaryText = KurioColors.DarkSecondaryText,
            accent = KurioColors.DarkAccent,
            success = KurioColors.DarkSuccess,
            error = KurioColors.DarkError,
            border = KurioColors.DarkBorder
        )
    } else {
        KurioColorScheme(
            background = KurioColors.Background,
            surface = KurioColors.Surface,
            secondarySurface = KurioColors.SecondarySurface,
            primaryText = KurioColors.PrimaryText,
            secondaryText = KurioColors.SecondaryText,
            accent = KurioColors.Accent,
            success = KurioColors.Success,
            error = KurioColors.Error,
            border = KurioColors.Border
        )
    }
}

data class KurioAlpha(
    val disabled: Float = 0.38f,
    val medium: Float = 0.6f,
    val high: Float = 0.87f
)
