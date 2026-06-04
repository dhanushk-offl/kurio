package com.dhanu.kurio.presentation.component.card

import androidx.compose.foundation.layout.*
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.design.spacing.KurioSpacing

@Composable
fun KurioCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .shadow(
                elevation = KurioSpacing.Xs,
                shape = MaterialTheme.shapes.large,
                spotColor = KurioColors.Primary.copy(alpha = 0.06f),
                ambientColor = KurioColors.Primary.copy(alpha = 0.04f)
            ),
        shape = MaterialTheme.shapes.large,
        colors = CardDefaults.cardColors(
            containerColor = KurioColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(KurioSpacing.Xl),
            content = content
        )
    }
}

@Composable
fun KurioStatCard(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    KurioCard(modifier = modifier) {
        Text(
            text = label,
            color = KurioColors.SecondaryText,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Spacer(Modifier.height(KurioSpacing.Xs))
        Text(
            text = value,
            color = KurioColors.PrimaryText,
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
    }
}
