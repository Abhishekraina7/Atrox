package com.example.atrox.ui.auth.onboarding

sealed interface Onboarding3Event {
    object NavigateBack : Onboarding3Event
    object NavigateToNext : Onboarding3Event
    object NavigateToSkip : Onboarding3Event
}
