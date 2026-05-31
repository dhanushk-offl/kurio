package com.dhanu.kurio.presentation.component.card

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors

@Composable
fun KurioCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit
) {
    androidx.compose.material3.Card(
        modifier = modifier
            .shadow(2.dp, RoundedCornerShape(16.dp), spotColor = KurioColors.Accent.copy(alpha = 0.08f)),
        shape = RoundedCornerShape(16.dp),
        colors = androidx.compose.material3.CardDefaults.cardColors(
            containerColor = KurioColors.Surface
        )
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
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
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            letterSpacing = 0.5.sp
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = value,
            color = KurioColors.PrimaryText,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold
        )
    }
}
