package com.example.atrox.ui.auth.login

sealed class LoginEvent {
    object NavigateToOnboarding : LoginEvent()
    object NavigateToRegister : LoginEvent()
    object NavigateToForgotPassword : LoginEvent()
}
