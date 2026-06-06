package com.dhanu.kurio.presentation.component.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.dhanu.kurio.core.design.color.KurioColors
import com.dhanu.kurio.core.model.OnboardingStep
import com.dhanu.kurio.presentation.component.button.KurioButton
import com.dhanu.kurio.presentation.component.button.KurioOutlinedButton

@Composable
fun OnboardingScreen(
    onComplete: () -> Unit,
    onRequestMicrophonePermission: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {}
) {
    var currentStep by remember { mutableStateOf(OnboardingStep.WELCOME) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(KurioColors.Background)
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(Modifier.weight(1f))

        when (currentStep) {
            OnboardingStep.WELCOME -> WelcomeStep(onNext = { currentStep = OnboardingStep.PERMISSIONS })
            OnboardingStep.PERMISSIONS -> PermissionsStep(
                onNext = { currentStep = OnboardingStep.MODEL_DOWNLOAD },
                onRequestMicrophonePermission = onRequestMicrophonePermission,
                onRequestOverlayPermission = onRequestOverlayPermission
            )
            OnboardingStep.MODEL_DOWNLOAD -> ModelDownloadStep(onNext = { currentStep = OnboardingStep.COMPLETION })
            OnboardingStep.COMPLETION -> CompletionStep(onComplete = onComplete)
        }

        Spacer(Modifier.weight(1f))

        StepIndicator(currentStep = currentStep)
        
        Spacer(Modifier.height(32.dp))
    }
}

@Composable
private fun WelcomeStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingMark("K")
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Welcome to Kurio",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Speak. Transcribe. Continue.\nYour speech, your device, always private.",
            fontSize = 16.sp,
            color = KurioColors.SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(40.dp))
        KurioButton(text = "Get Started", onClick = onNext)
    }
}

@Composable
private fun PermissionsStep(
    onNext: () -> Unit,
    onRequestMicrophonePermission: () -> Unit = {},
    onRequestOverlayPermission: () -> Unit = {}
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingMark("Perm")
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Permissions",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Kurio needs microphone access to transcribe your speech. Overlay permission enables transcription anywhere. All audio stays on your device.",
            fontSize = 15.sp,
            color = KurioColors.SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(32.dp))
        KurioButton(text = "Allow Microphone", onClick = onRequestMicrophonePermission)
        Spacer(Modifier.height(12.dp))
        KurioOutlinedButton(text = "Allow Overlay", onClick = onRequestOverlayPermission)
        Spacer(Modifier.height(12.dp))
        TextButton(onClick = onNext) {
            Text("Skip", color = KurioColors.SecondaryText)
        }
    }
}

@Composable
private fun ModelDownloadStep(onNext: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingMark("Model")
        Spacer(Modifier.height(24.dp))
        Text(
            text = "Download a Model",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Choose a speech recognition model to get started. The Whisper Tiny English model is recommended for fast, lightweight transcription.",
            fontSize = 15.sp,
            color = KurioColors.SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )
        Spacer(Modifier.height(32.dp))
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = KurioColors.Surface),
            modifier = Modifier
                .fillMaxWidth()
                .shadow(2.dp, RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("Whisper Tiny English", fontWeight = FontWeight.SemiBold, fontSize = 16.sp, color = KurioColors.PrimaryText)
                Spacer(Modifier.height(4.dp))
                Text("~39 MB • Recommended", fontSize = 13.sp, color = KurioColors.SecondaryText)
            }
        }
        Spacer(Modifier.height(24.dp))
        KurioButton(text = "Continue", onClick = onNext)
        Spacer(Modifier.height(12.dp))
        KurioOutlinedButton(text = "Skip for Now", onClick = onNext)
    }
}

@Composable
private fun CompletionStep(onComplete: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OnboardingMark("Ready")
        Spacer(Modifier.height(24.dp))
        Text(
            text = "You're Ready!",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.PrimaryText,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(12.dp))
        Text(
            text = "Tap the microphone to start transcribing.\nTap again to stop and see your results.",
            fontSize = 16.sp,
            color = KurioColors.SecondaryText,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp
        )
        Spacer(Modifier.height(40.dp))
        KurioButton(text = "Start Using Kurio", onClick = onComplete)
    }
}

@Composable
private fun StepIndicator(currentStep: OnboardingStep) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        OnboardingStep.entries.forEach { step ->
            val isActive = step.ordinal <= currentStep.ordinal
            Box(
                modifier = Modifier
                    .size(if (step == currentStep) 10.dp else 8.dp)
                    .shadow(if (isActive) 2.dp else 0.dp, CircleShape)
                    .background(
                        if (isActive) KurioColors.Accent
                        else KurioColors.Border,
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun OnboardingMark(text: String) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .shadow(2.dp, CircleShape)
            .background(KurioColors.Surface, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = KurioColors.Primary
        )
    }
}
