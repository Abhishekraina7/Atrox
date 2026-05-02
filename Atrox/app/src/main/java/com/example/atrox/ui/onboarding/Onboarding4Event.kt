package com.example.atrox.ui.onboarding

sealed class Onboarding4Event {
    object NavigateBack : Onboarding4Event()
    object NavigateToDashboard : Onboarding4Event()
    object NavigateToSkip : Onboarding4Event()
}
