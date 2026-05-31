package com.dhanu.kurio.presentation.component.textfield

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors

@Composable
fun KurioTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    label: String = "",
    placeholder: String = "",
    enabled: Boolean = true,
    readOnly: Boolean = false,
    maxLines: Int = 1,
    trailingIcon: @Composable (() -> Unit)? = null
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        modifier = modifier.fillMaxWidth(),
        enabled = enabled,
        readOnly = readOnly,
        maxLines = maxLines,
        label = if (label.isNotEmpty()) {
            { Text(label, fontSize = 14.sp, fontWeight = FontWeight.Medium) }
        } else null,
        placeholder = if (placeholder.isNotEmpty()) {
            { Text(placeholder, color = KurioColors.SecondaryText.copy(alpha = 0.6f)) }
        } else null,
        trailingIcon = trailingIcon,
        shape = RoundedCornerShape(12.dp),
        colors = OutlinedTextFieldDefaults.colors(
            focusedTextColor = KurioColors.PrimaryText,
            unfocusedTextColor = KurioColors.PrimaryText,
            cursorColor = KurioColors.Accent,
            focusedBorderColor = KurioColors.Accent,
            unfocusedBorderColor = KurioColors.Border,
            focusedLabelColor = KurioColors.Accent,
            unfocusedLabelColor = KurioColors.SecondaryText,
            focusedContainerColor = KurioColors.Surface,
            unfocusedContainerColor = KurioColors.Surface
        )
    )
}
