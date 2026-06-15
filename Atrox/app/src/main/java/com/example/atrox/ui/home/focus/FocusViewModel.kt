package com.example.atrox.ui.home.focus

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.tasks.TaskItem
import com.example.atrox.data.tasks.TaskRepository
import com.example.atrox.service.regulator.RegulatorRepository
import com.example.atrox.service.regulator.RegulatorManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class TimerState { IDLE, RUNNING, PAUSED, FINISHED }

data class FocusUiState(
    val task: TaskItem? = null,
    val totalSeconds: Int = 0,
    val remainingSeconds: Int = 0,
    val timerState: TimerState = TimerState.IDLE,
    val progressFraction: Float = 1f,
    val guardianPhone: String? = null,
    val isCancelRequestSent: Boolean = false,
    val isApproved: Boolean = false
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val repository: TaskRepository,
    private val regulatorRepository: RegulatorRepository,
    private val regulatorManager: RegulatorManager,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(FocusUiState())
    val uiState = _uiState.asStateFlow()

    private var timerJob: Job? = null

    init {
        viewModelScope.launch {
            val phone = regulatorRepository.guardianPhone.first()
            val task = repository.tasks.first().firstOrNull { it.id == taskId }
            if (task != null) {
                val totalSeconds = task.durationMin * 60
                _uiState.value = FocusUiState(
                    task = task,
                    totalSeconds = totalSeconds,
                    remainingSeconds = totalSeconds,
                    timerState = TimerState.IDLE,
                    progressFraction = 1f,
                    guardianPhone = phone
                )
            }
        }
        viewModelScope.launch {
            regulatorManager.approvalEvents.collect {
                _uiState.value = _uiState.value.copy(isApproved = true)
                pauseTimer()
            }
        }
    }

    fun startTimer() {
        if (_uiState.value.timerState == TimerState.RUNNING) return
        _uiState.value = _uiState.value.copy(timerState = TimerState.RUNNING)
        timerJob = viewModelScope.launch {
            while (_uiState.value.remainingSeconds > 0) {
                delay(1000L)
                val newRemaining = _uiState.value.remainingSeconds - 1
                val total = _uiState.value.totalSeconds.toFloat()
                _uiState.value = _uiState.value.copy(
                    remainingSeconds = newRemaining,
                    progressFraction = if (total > 0) newRemaining / total else 0f
                )
            }
            _uiState.value = _uiState.value.copy(timerState = TimerState.FINISHED)
        }
    }

    fun pauseTimer() {
        timerJob?.cancel()
        _uiState.value = _uiState.value.copy(timerState = TimerState.PAUSED)
    }

    fun resetTimer() {
        timerJob?.cancel()
        val total = _uiState.value.totalSeconds
        _uiState.value = _uiState.value.copy(
            remainingSeconds = total,
            progressFraction = 1f,
            timerState = TimerState.IDLE
        )
    }

    fun markRequestSent() {
        _uiState.value = _uiState.value.copy(isCancelRequestSent = true)
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
    }

    // Formats remaining seconds to MM:SS
    fun formatTime(seconds: Int): String {
        val m = seconds / 60
        val s = seconds % 60
        return "%02d:%02d".format(m, s)
    }
}
