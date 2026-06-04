package com.dhanu.kurio.presentation.component.screen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.spacing.KurioSpacing

@Composable
fun KurioScreen(
    title: String,
    subtitle: String? = null,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .padding(horizontal = KurioSpacing.ScreenHorizontal)
    ) {
        Spacer(Modifier.height(KurioSpacing.ScreenTop))
        Text(
            text = title,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText
        )
        if (subtitle != null) {
            Spacer(Modifier.height(KurioSpacing.Xs))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = KurioColors.SecondaryText
            )
        }
        Spacer(Modifier.height(KurioSpacing.Xxl))
        content()
    }
}
