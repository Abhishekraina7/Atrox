package com.example.atrox.ui.onboarding

sealed interface OnboardingEvent {
    object NavigateToNext : OnboardingEvent
    object NavigateToLogin : OnboardingEvent
}
