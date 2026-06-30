package com.example.atrox.ui.home.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusBreakUiState(
    val remainingSeconds: Int = 5 * 60, // 5 minutes
    val selectedAtmosphere: String = "Rain",
    val completedSprints: Int = 2,
    val totalSprints: Int = 4,
    val tasksCompleted: Int = 12,
    val focusMinutes: Int = 50,
    val focusSeconds: Int = 0,
    val dailyProgressPercent: Int = 75,
    val isFinished: Boolean = false
)

@HiltViewModel
class FocusBreakViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(FocusBreakUiState())
    val uiState: StateFlow<FocusBreakUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                val current = _uiState.value
                if (current.remainingSeconds > 0) {
                    _uiState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                } else {
                    _uiState.value = current.copy(isFinished = true)
                    break
                }
            }
        }
    }

    fun selectAtmosphere(atmosphere: String) {
        _uiState.value = _uiState.value.copy(selectedAtmosphere = atmosphere)
    }

    fun skipBreak() {
        _uiState.value = _uiState.value.copy(isFinished = true)
        timerJob?.cancel()
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
