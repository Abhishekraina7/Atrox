package com.example.atrox.ui.auth.onboarding

sealed interface OnboardingEvent {
    object NavigateToNext : OnboardingEvent
    object NavigateToLogin : OnboardingEvent
}
