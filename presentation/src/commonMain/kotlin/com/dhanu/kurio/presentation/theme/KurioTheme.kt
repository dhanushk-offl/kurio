package com.dhanu.kurio.presentation.theme

import androidx.compose.runtime.Composable
import com.dhanu.kurio.core.design.color.KurioAlpha
import com.dhanu.kurio.core.design.color.KurioColorScheme
import com.dhanu.kurio.core.design.color.kurioColorScheme

object KurioTheme {
    val colors: KurioColorScheme
        @Composable get() = kurioColorScheme()

    val alpha: KurioAlpha
        get() = KurioAlpha()
}
