package com.dhanu.kurio.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.typography.KurioTypography

private val LightColorScheme = lightColorScheme(
    primary = KurioColors.Accent,
    onPrimary = KurioColors.White,
    primaryContainer = KurioColors.SecondarySurface,
    onPrimaryContainer = KurioColors.PrimaryText,
    secondary = KurioColors.SecondaryText,
    onSecondary = KurioColors.White,
    background = KurioColors.Background,
    onBackground = KurioColors.PrimaryText,
    surface = KurioColors.Surface,
    onSurface = KurioColors.PrimaryText,
    surfaceVariant = KurioColors.SecondarySurface,
    onSurfaceVariant = KurioColors.SecondaryText,
    error = KurioColors.Error,
    onError = KurioColors.White,
    outline = KurioColors.Border,
    outlineVariant = KurioColors.Border.copy(alpha = 0.5f)
)

private val DarkColorScheme = darkColorScheme(
    primary = KurioColors.DarkAccent,
    onPrimary = KurioColors.Black,
    primaryContainer = KurioColors.DarkSecondarySurface,
    onPrimaryContainer = KurioColors.DarkPrimaryText,
    secondary = KurioColors.DarkSecondaryText,
    onSecondary = KurioColors.Black,
    background = KurioColors.DarkBackground,
    onBackground = KurioColors.DarkPrimaryText,
    surface = KurioColors.DarkSurface,
    onSurface = KurioColors.DarkPrimaryText,
    surfaceVariant = KurioColors.DarkSecondarySurface,
    onSurfaceVariant = KurioColors.DarkSecondaryText,
    error = KurioColors.DarkError,
    onError = KurioColors.Black,
    outline = KurioColors.DarkBorder,
    outlineVariant = KurioColors.DarkBorder.copy(alpha = 0.5f)
)

private val KurioShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(16.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(24.dp)
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
