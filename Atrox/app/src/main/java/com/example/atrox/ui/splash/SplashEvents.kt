package com.example.atrox.ui.splash

sealed interface SplashEvent {
    object NavigateToOnboarding : SplashEvent
    object NavigateToLogin : SplashEvent
    object NavigateToHome : SplashEvent
}   