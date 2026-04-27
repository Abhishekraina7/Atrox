package com.example.atrox.ui.auth.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onSignInClicked() {
        // Authenticate user, then...
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToOnboarding)
        }
    }

    fun onGoogleSignInClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToOnboarding)
        }
    }

    fun onAppleSignInClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToOnboarding)
        }
    }

    fun onForgotPasswordClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToForgotPassword)
        }
    }

    fun onCreateAccountClicked() {
        viewModelScope.launch {
            _events.emit(LoginEvent.NavigateToRegister)
        }
    }
}
