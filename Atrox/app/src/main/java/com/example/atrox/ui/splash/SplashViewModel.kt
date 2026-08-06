package com.example.atrox.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import dagger.Lazy
import com.google.firebase.auth.FirebaseAuth
import com.example.atrox.data.local.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.first

import com.example.atrox.domain.sync.CloudSyncManager

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val preferences: UserPreferencesRepository,
    private val firebaseAuth: Lazy<FirebaseAuth>,
    private val cloudSyncManager: CloudSyncManager
) : ViewModel() {

    // MutableSharedFlow is used over StateFlow because navigation is a "one-shot" event.
    // We want the event to be consumed exactly once, rather than holding state.
    private val _events = MutableSharedFlow<SplashEvent>()
    val events = _events.asSharedFlow()

    private val _progress = MutableStateFlow(0f)
    val progress = _progress.asStateFlow()

    init {
        startCalibration()
    }

    private fun startCalibration() {
        viewModelScope.launch {
            // 1. Initialize checks
            _progress.value = 0.2f
            
            // 2. Check Authentication State (Firebase)
            val currentUser = firebaseAuth.get().currentUser
            _progress.value = 0.5f
            
            // 3. Verify Local Configurations
            val primaryGoal = preferences.primaryGoal.first()
            _progress.value = 0.8f
            
            // 4. Finalize
            _progress.value = 1.0f
            delay(150) // Tiny pause so the progress bar visually completes
            
            // 5. Route based on real application state
            if (currentUser == null) {
                _events.emit(SplashEvent.NavigateToLogin)
            } else if (primaryGoal.isEmpty()) {
                // Authenticated, but hasn't completed onboarding profile
                cloudSyncManager.sync()
                _events.emit(SplashEvent.NavigateToOnboarding)
            } else {
                // Authenticated and fully set up
                cloudSyncManager.sync()
                _events.emit(SplashEvent.NavigateToHome)
            }
        }
    }
}