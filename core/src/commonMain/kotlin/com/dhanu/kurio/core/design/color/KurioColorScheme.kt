package com.dhanu.kurio.core.design.color

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

data class KurioColorScheme(
    val background: Color,
    val surface: Color,
    val surfaceVariant: Color,
    val primaryText: Color,
    val secondaryText: Color,
    val accent: Color,
    val success: Color,
    val error: Color,
    val warning: Color,
    val border: Color,
    val divider: Color,
    val surfaceContainer: Color
)

fun kurioColorScheme(darkTheme: Boolean): KurioColorScheme {
    return if (darkTheme) {
        KurioColorScheme(
            background = KurioColors.DarkBackground,
            surface = KurioColors.DarkSurface,
            surfaceVariant = KurioColors.DarkSurfaceVariant,
            primaryText = KurioColors.DarkPrimaryText,
            secondaryText = KurioColors.DarkSecondaryText,
            accent = KurioColors.DarkAccent,
            success = KurioColors.DarkSuccess,
            error = KurioColors.DarkError,
            warning = KurioColors.DarkWarning,
            border = KurioColors.DarkBorder,
            divider = KurioColors.DarkDivider,
            surfaceContainer = KurioColors.DarkSurfaceContainer
        )
    } else {
        KurioColorScheme(
            background = KurioColors.Background,
            surface = KurioColors.Surface,
            surfaceVariant = KurioColors.SurfaceVariant,
            primaryText = KurioColors.PrimaryText,
            secondaryText = KurioColors.SecondaryText,
            accent = KurioColors.Accent,
            success = KurioColors.Success,
            error = KurioColors.Error,
            warning = KurioColors.Warning,
            border = KurioColors.Border,
            divider = KurioColors.Divider,
            surfaceContainer = KurioColors.SurfaceContainer
        )
    }
}
