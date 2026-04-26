package com.example.atrox.ui.auth.onboarding

sealed interface Onboarding2Event {
    object NavigateBack : Onboarding2Event
    object NavigateToNext : Onboarding2Event // E.g., Onboarding3 or Home
    object NavigateToSkip : Onboarding2Event // E.g., skip setup and go to Home
}
