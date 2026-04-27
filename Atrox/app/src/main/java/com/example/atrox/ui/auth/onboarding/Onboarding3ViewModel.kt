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
class Onboarding3ViewModel @Inject constructor() : ViewModel() {

    private val _events = MutableSharedFlow<Onboarding3Event>()
    val events = _events.asSharedFlow()

    private val _sprintDuration = MutableStateFlow(25)
    val sprintDuration = _sprintDuration.asStateFlow()

    private val _breakDuration = MutableStateFlow(5)
    val breakDuration = _breakDuration.asStateFlow()

    private val _dailySprints = MutableStateFlow(8)
    val dailySprints = _dailySprints.asStateFlow()

    fun onSprintDurationSelected(duration: Int) {
        _sprintDuration.value = duration
    }

    fun onBreakDurationSelected(duration: Int) {
        _breakDuration.value = duration
    }

    fun onIncrementSprints() {
        if (_dailySprints.value < 20) {
            _dailySprints.value += 1
        }
    }

    fun onDecrementSprints() {
        if (_dailySprints.value > 1) {
            _dailySprints.value -= 1
        }
    }

    fun onBackClicked() {
        viewModelScope.launch { _events.emit(Onboarding3Event.NavigateBack) }
    }

    fun onContinueClicked() {
        viewModelScope.launch { _events.emit(Onboarding3Event.NavigateToNext) }
    }

    fun onSkipClicked() {
        viewModelScope.launch { _events.emit(Onboarding3Event.NavigateToSkip) }
    }
}
