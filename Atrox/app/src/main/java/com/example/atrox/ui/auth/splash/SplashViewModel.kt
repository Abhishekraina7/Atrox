package com.example.atrox.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    // Normally you would inject your UserPreferences DataStore repository here
    // private val preferences: UserPreferencesRepository
) : ViewModel() {

    // MutableSharedFlow is used over StateFlow because navigation is a "one-shot" event.
    // We want the event to be consumed exactly once, rather than holding state.
    private val _events = MutableSharedFlow<SplashEvent>()
    val events = _events.asSharedFlow()

    init {
        startCalibration()
    }

    private fun startCalibration() {
        viewModelScope.launch {
            // 1. Emulate some initial work and waiting (2 seconds limit)
            delay(20000)
            
            // 2. Here, you would read from your DataStore:
            // val isFirstLaunch = preferences.isFirstLaunch()
            // val isLoggedIn = preferences.isLoggedIn()
            
            // 3. For now, we'll route to Onboarding as a default
            // In the future:
            // if (isFirstLaunch) _events.emit(SplashEvent.NavigateToOnboarding)
            // else if (!isLoggedIn) _events.emit(SplashEvent.NavigateToLogin)
            // else _events.emit(SplashEvent.NavigateToHome)
            
            _events.emit(SplashEvent.NavigateToOnboarding)
        }
    }
}