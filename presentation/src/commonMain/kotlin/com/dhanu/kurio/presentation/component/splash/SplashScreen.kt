package com.dhanu.kurio.presentation.component.splash

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import kotlinx.coroutines.delay

@Composable
fun AnimatedSplashScreen(
    onSplashComplete: () -> Unit
) {
    var startExit by remember { mutableStateOf(false) }

    val logoScale = animateFloatAsState(
        targetValue = if (startExit) 0.6f else 1f,
        animationSpec = tween(500, easing = FastOutSlowInEasing)
    )
    val logoAlpha = animateFloatAsState(
        targetValue = if (startExit) 0f else 1f,
        animationSpec = tween(500)
    )

    val titleOffset = animateFloatAsState(
        targetValue = if (startExit) -20f else 0f,
        animationSpec = tween(400, delayMillis = 100)
    )
    val titleAlpha = animateFloatAsState(
        targetValue = if (startExit) 0f else 1f,
        animationSpec = tween(400, delayMillis = 100)
    )

    val taglineAlpha = animateFloatAsState(
        targetValue = if (startExit) 0f else 1f,
        animationSpec = tween(400, delayMillis = 250)
    )

    val containerAlpha = animateFloatAsState(
        targetValue = if (startExit) 0f else 1f,
        animationSpec = tween(400)
    )

    LaunchedEffect(Unit) {
        delay(650)
        startExit = true
        delay(300)
        onSplashComplete()
    }

    if (containerAlpha.value <= 0f) return

    Box(
        modifier = Modifier
            .fillMaxSize()
            .alpha(containerAlpha.value)
            .background(KurioColors.Background),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(32.dp)
        ) {
            Spacer(Modifier.weight(1f))

            Box(
                modifier = Modifier
                    .size(100.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
                    .clip(CircleShape)
                    .background(KurioColors.Primary.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "K",
                    fontSize = 44.sp,
                    fontWeight = FontWeight.Bold,
                    color = KurioColors.Primary
                )
            }

            Spacer(Modifier.height(24.dp))

            Text(
                text = "Kurio",
                fontSize = 42.sp,
                fontWeight = FontWeight.Bold,
                color = KurioColors.PrimaryText,
                modifier = Modifier
                    .alpha(titleAlpha.value)
                    .offset(y = titleOffset.value.dp)
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = "Speak. Transcribe. Continue.",
                fontSize = 15.sp,
                color = KurioColors.SecondaryText,
                textAlign = TextAlign.Center,
                modifier = Modifier.alpha(taglineAlpha.value)
            )

            Spacer(Modifier.weight(1f))

            Text(
                text = "Powered by Whisper.cpp",
                fontSize = 12.sp,
                color = KurioColors.SecondaryText.copy(alpha = 0.4f),
                modifier = Modifier
                    .alpha(taglineAlpha.value * 0.6f)
                    .padding(bottom = 48.dp)
            )
        }
    }
}
