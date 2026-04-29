package com.example.atrox.ui.auth.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.atrox.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository
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
            delay(2000)
            
            // 2. Read from DataStore:
            val isLoggedIn = preferences.isLoggedIn.first()
            
            // 3. Route accordingly
            if (isLoggedIn) {
                _events.emit(SplashEvent.NavigateToOnboarding)
            } else {
                _events.emit(SplashEvent.NavigateToLogin)
            }
        }
    }
}