package com.dhanu.kurio.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.shape.KurioShapes
import com.dhanu.kurio.core.design.typography.KurioTypography

private val LightColorScheme = lightColorScheme(
    primary = KurioColors.Primary,
    onPrimary = KurioColors.OnPrimary,
    primaryContainer = KurioColors.Primary.copy(alpha = 0.12f),
    onPrimaryContainer = KurioColors.PrimaryVariant,
    secondary = KurioColors.Secondary,
    onSecondary = KurioColors.OnSecondary,
    secondaryContainer = KurioColors.SurfaceVariant,
    onSecondaryContainer = KurioColors.SecondaryVariant,
    background = KurioColors.Background,
    onBackground = KurioColors.PrimaryText,
    surface = KurioColors.Surface,
    onSurface = KurioColors.PrimaryText,
    surfaceVariant = KurioColors.SurfaceVariant,
    onSurfaceVariant = KurioColors.SecondaryText,
    error = KurioColors.Error,
    onError = KurioColors.White,
    outline = KurioColors.Border,
    outlineVariant = KurioColors.Divider,
    surfaceTint = KurioColors.Primary,
    inverseSurface = KurioColors.DarkSurface,
    inverseOnSurface = KurioColors.DarkPrimaryText,
    inversePrimary = KurioColors.DarkPrimary
)

private val DarkColorScheme = darkColorScheme(
    primary = KurioColors.DarkPrimary,
    onPrimary = KurioColors.DarkOnPrimary,
    primaryContainer = KurioColors.DarkPrimary.copy(alpha = 0.12f),
    onPrimaryContainer = KurioColors.DarkPrimary,
    secondary = KurioColors.DarkSecondary,
    onSecondary = KurioColors.DarkOnSecondary,
    secondaryContainer = KurioColors.DarkSurfaceVariant,
    onSecondaryContainer = KurioColors.DarkSecondary,
    background = KurioColors.DarkBackground,
    onBackground = KurioColors.DarkPrimaryText,
    surface = KurioColors.DarkSurface,
    onSurface = KurioColors.DarkPrimaryText,
    surfaceVariant = KurioColors.DarkSurfaceVariant,
    onSurfaceVariant = KurioColors.DarkSecondaryText,
    error = KurioColors.DarkError,
    onError = KurioColors.Black,
    outline = KurioColors.DarkBorder,
    outlineVariant = KurioColors.DarkDivider,
    surfaceTint = KurioColors.DarkPrimary,
    inverseSurface = KurioColors.Surface,
    inverseOnSurface = KurioColors.PrimaryText,
    inversePrimary = KurioColors.Primary
)

@Composable
fun KurioTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KurioTypography,
        shapes = KurioShapes,
        content = content
    )
}
