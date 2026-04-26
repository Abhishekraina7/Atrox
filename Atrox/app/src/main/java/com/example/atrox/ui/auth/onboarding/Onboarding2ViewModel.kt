package com.example.atrox.ui.auth.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class Onboarding2ViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<Onboarding2Event>()
    val events = _events.asSharedFlow()

    private val _selectedGoal = MutableStateFlow("Deep Work")
    val selectedGoal = _selectedGoal.asStateFlow()

    private val _targetHours = MutableStateFlow(4f)
    val targetHours = _targetHours.asStateFlow()

    fun onGoalSelected(goal: String) {
        _selectedGoal.value = goal
    }

    fun onHoursChanged(hours: Float) {
        _targetHours.value = hours
    }

    fun onBackClicked() {
        viewModelScope.launch { _events.emit(Onboarding2Event.NavigateBack) }
    }

    fun onContinueClicked() {
        viewModelScope.launch { _events.emit(Onboarding2Event.NavigateToNext) }
    }

    fun onSkipClicked() {
        viewModelScope.launch { _events.emit(Onboarding2Event.NavigateToSkip) }
    }
}
