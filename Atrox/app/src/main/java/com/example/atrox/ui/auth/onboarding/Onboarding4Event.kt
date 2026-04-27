package com.example.atrox.ui.auth.onboarding

sealed class Onboarding4Event {
    object NavigateBack : Onboarding4Event()
    object NavigateToDashboard : Onboarding4Event()
    object NavigateToSkip : Onboarding4Event()
}
