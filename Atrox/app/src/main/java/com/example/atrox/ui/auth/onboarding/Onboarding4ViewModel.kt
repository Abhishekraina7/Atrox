package com.example.atrox.ui.auth.onboarding

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
class Onboarding4ViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<Onboarding4Event>()
    val events: SharedFlow<Onboarding4Event> = _events.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        _searchQuery.value = query
    }

    fun onBackClicked() {
        emitEvent(Onboarding4Event.NavigateBack)
    }

    fun onSkipClicked() {
        emitEvent(Onboarding4Event.NavigateToSkip)
    }

    fun onContinueClicked() {
        emitEvent(Onboarding4Event.NavigateToDashboard)
    }

    private fun emitEvent(event: Onboarding4Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
