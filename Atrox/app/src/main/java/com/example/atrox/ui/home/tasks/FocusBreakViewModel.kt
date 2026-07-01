package com.example.atrox.ui.home.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
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
    val isFinished: Boolean = false,
    val nextTaskId: String? = null,
    val isAutoStartEnabled: Boolean = true
)

@HiltViewModel
class FocusBreakViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusBreakUiState())
    val uiState: StateFlow<FocusBreakUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        startTimer()
        viewModelScope.launch {
            preferencesRepository.autoStartNextSprint.collect { autoStart ->
                _uiState.value = _uiState.value.copy(isAutoStartEnabled = autoStart)
            }
        }
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
                    finishBreak()
                    break
                }
            }
        }
    }

    private suspend fun finishBreak() {
        val autoStart = preferencesRepository.autoStartNextSprint.firstOrNull() ?: true
        if (autoStart) {
            val tasks = taskRepository.tasks.firstOrNull() ?: emptyList()
            val nextTask = tasks.firstOrNull { !it.isCompleted }
            _uiState.value = _uiState.value.copy(
                isFinished = true,
                nextTaskId = nextTask?.id
            )
        } else {
            _uiState.value = _uiState.value.copy(isFinished = true, nextTaskId = null)
        }
    }

    fun selectAtmosphere(atmosphere: String) {
        _uiState.value = _uiState.value.copy(selectedAtmosphere = atmosphere)
    }

    fun toggleAutoStart() {
        val current = _uiState.value.isAutoStartEnabled
        viewModelScope.launch {
            preferencesRepository.setAutoStartNextSprint(!current)
        }
    }

    fun skipBreak() {
        timerJob?.cancel()
        viewModelScope.launch {
            finishBreak()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }
}
