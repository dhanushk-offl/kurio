package com.dhanu.kurio.core.model

import kotlinx.serialization.Serializable

@Serializable
enum class OnboardingStep {
    WELCOME,
    PERMISSIONS,
    MODEL_DOWNLOAD,
    COMPLETION
}

@Serializable
data class OnboardingState(
    val completed: Boolean = false,
    val currentStep: OnboardingStep = OnboardingStep.WELCOME,
    val permissionsGranted: Boolean = false,
    val modelDownloaded: Boolean = false
)
