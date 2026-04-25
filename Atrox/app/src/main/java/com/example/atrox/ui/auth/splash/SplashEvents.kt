package com.example.atrox.ui.auth.splash

sealed interface SplashEvent {
    object NavigateToOnboarding : SplashEvent
    object NavigateToLogin : SplashEvent
    object NavigateToHome : SplashEvent
}   