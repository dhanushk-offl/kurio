package com.dhanu.kurio.presentation.component.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors

@Composable
fun KurioDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit,
    confirmText: String = "Confirm",
    dismissText: String = "Cancel",
    isDestructive: Boolean = false
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = KurioColors.Surface,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = KurioColors.PrimaryText
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = KurioColors.SecondaryText,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text(
                    text = confirmText,
                    color = if (isDestructive) KurioColors.Error else KurioColors.Accent,
                    fontWeight = FontWeight.Medium
                )
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = dismissText,
                    color = KurioColors.SecondaryText,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}

@Composable
fun KurioInfoDialog(
    title: String,
    message: String,
    onDismiss: () -> Unit,
    buttonText: String = "Got it"
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(20.dp),
        containerColor = KurioColors.Surface,
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.SemiBold,
                fontSize = 18.sp,
                color = KurioColors.PrimaryText
            )
        },
        text = {
            Text(
                text = message,
                fontSize = 14.sp,
                color = KurioColors.SecondaryText,
                lineHeight = 22.sp
            )
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(
                    text = buttonText,
                    color = KurioColors.Accent,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    )
}
