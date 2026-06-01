package com.dhanu.kurio.presentation.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.staticCompositionLocalOf
import com.dhanu.kurio.core.design.color.KurioAlpha
import com.dhanu.kurio.core.design.color.KurioColorScheme
import com.dhanu.kurio.core.design.color.kurioColorScheme

val LocalKurioColorScheme = staticCompositionLocalOf {
    kurioColorScheme(darkTheme = false)
}

object KurioTheme {
    val colors: KurioColorScheme
        @Composable get() = LocalKurioColorScheme.current

    val alpha: KurioAlpha
        get() = KurioAlpha()
}
