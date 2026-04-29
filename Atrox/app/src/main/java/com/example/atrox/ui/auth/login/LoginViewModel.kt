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
import com.example.atrox.data.auth.AuthRepository
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.data.auth.UserRepository
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val userRepository: UserRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<LoginEvent>()
    val events: SharedFlow<LoginEvent> = _events.asSharedFlow()

    private val _isRegisterMode = MutableStateFlow(false)
    val isRegisterMode: StateFlow<Boolean> = _isRegisterMode.asStateFlow()

    private val _username = MutableStateFlow("")
    val username: StateFlow<String> = _username.asStateFlow()

    private val _email = MutableStateFlow("")
    val email: StateFlow<String> = _email.asStateFlow()

    private val _password = MutableStateFlow("")
    val password: StateFlow<String> = _password.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    fun toggleRegisterMode() {
        _isRegisterMode.value = !_isRegisterMode.value
        _errorMessage.value = null
    }

    fun onUsernameChanged(username: String) {
        _username.value = username
    }

    fun onEmailChanged(email: String) {
        _email.value = email
    }

    fun onPasswordChanged(password: String) {
        _password.value = password
    }

    fun onSignInClicked() {
        if (_email.value.isBlank() || _password.value.isBlank() || (_isRegisterMode.value && _username.value.isBlank())) {
            _errorMessage.value = "Please fill in all fields"
            return
        }

        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            if (_isRegisterMode.value) {
                // Registration Flow
                val result = authRepository.createUserWithEmailAndPassword(_email.value, _password.value)
                result.onSuccess { authResult ->
                    val user = authResult.user
                    if (user != null) {
                        userRepository.saveUserToDatabase(
                            uid = user.uid,
                            email = _email.value,
                            username = _username.value
                        )
                    }
                    userPreferencesRepository.setLoggedIn(true)
                    _events.emit(LoginEvent.NavigateToOnboarding)
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Registration failed"
                }
            } else {
                // Sign In Flow
                val result = authRepository.signInWithEmailAndPassword(_email.value, _password.value)
                result.onSuccess {
                    userPreferencesRepository.setLoggedIn(true)
                    _events.emit(LoginEvent.NavigateToOnboarding)
                }.onFailure { exception ->
                    _errorMessage.value = exception.message ?: "Sign in failed"
                }
            }
            _isLoading.value = false
        }
    }

    fun onGoogleTokenReceived(idToken: String) {
        _isLoading.value = true
        _errorMessage.value = null
        
        viewModelScope.launch {
            val result = authRepository.signInWithGoogleCredential(idToken)
            result.onSuccess { authResult ->
                // Ensure user is in our Firestore db if it's their first time
                val user = authResult.user
                if (user != null) {
                    // Try to save, ignore failure as they are still authenticated
                    userRepository.saveUserToDatabase(
                        uid = user.uid,
                        email = user.email ?: "",
                        username = user.displayName ?: ""
                    )
                }
                
                // Save session local state
                userPreferencesRepository.setLoggedIn(true)
                _events.emit(LoginEvent.NavigateToOnboarding)
            }.onFailure { exception ->
                _errorMessage.value = exception.message ?: "Google Sign-in failed"
            }
            _isLoading.value = false
        }
    }

    fun onGoogleSignInClicked() {
        // This acts as a trigger fallback or UI state toggle if needed, 
        // but actual login flow is initiated from Compose UI since it requires Context.
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
        _isRegisterMode.value = true
    }
}
