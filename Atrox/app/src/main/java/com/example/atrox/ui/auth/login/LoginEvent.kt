package com.example.atrox.ui.auth.login

sealed class LoginEvent {
    object NavigateToOnboarding : LoginEvent()
    object NavigateToHome : LoginEvent()
    object NavigateToForgotPassword : LoginEvent()
}
