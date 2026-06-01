package com.dhanu.kurio.core.design.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Shapes
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.dhanu.kurio.core.design.color.KurioColors
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

private val KurioShapes = Shapes(
    small = androidx.compose.foundation.shape.RoundedCornerShape(4.dp),
    medium = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
    large = androidx.compose.foundation.shape.RoundedCornerShape(12.dp),
    extraLarge = androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
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
