package com.example.atrox.ui.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class OnboardingViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<OnboardingEvent>()
    val events = _events.asSharedFlow()

    fun onBeginClicked() {
        viewModelScope.launch {
            _events.emit(OnboardingEvent.NavigateToNext)
        }
    }

    fun onSignInClicked() {
        viewModelScope.launch {
            _events.emit(OnboardingEvent.NavigateToLogin)
        }
    }
}
