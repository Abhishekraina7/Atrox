package com.example.atrox.ui.home.tasks

import android.content.Context
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.Data
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atrox.data.tasks.TaskRepository
import com.example.atrox.service.regulator.RegulatorManager
import com.example.atrox.service.regulator.RegulatorRepository
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.service.worker.SendSmsWorker
import android.app.NotificationManager
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import javax.inject.Inject

data class FocusSessionUiState(
    val taskId: String = "",
    val taskName: String = "Loading...",
    val durationMin: Int = 25,
    val remainingSeconds: Int = 25 * 60,
    val isPaused: Boolean = false,
    val isFinished: Boolean = false,
    val regulatorName: String = "Marcus", // Using Marcus as per design
    val isWaitingForApproval: Boolean = false,
    val approvalMessage: String = "",
    val requireApproval: Boolean = false,
    val navigateToDashboard: Boolean = false,
    val hapticFeedbackEnabled: Boolean = true
)

@HiltViewModel
class FocusSessionViewModel @Inject constructor(
    private val savedStateHandle: SavedStateHandle,
    private val repository: TaskRepository,
    private val regulatorRepository: RegulatorRepository,
    private val preferencesRepository: UserPreferencesRepository,
    private val regulatorManager: RegulatorManager,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val taskId: String = checkNotNull(savedStateHandle["taskId"])

    private val _uiState = MutableStateFlow(FocusSessionUiState(taskId = taskId))
    val uiState: StateFlow<FocusSessionUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var originalInterruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL
    private var dndActivated = false

    init {
        loadTask()
        loadRegulator()
        observeApprovals()
    }

    private fun loadRegulator() {
        viewModelScope.launch {
            val name = regulatorRepository.guardianName.firstOrNull()
            if (!name.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(regulatorName = name)
            }
            val reqApproval = preferencesRepository.approvalForEarlyExit.firstOrNull() ?: false
            _uiState.value = _uiState.value.copy(requireApproval = reqApproval)

            val hapticEnabled = preferencesRepository.hapticFeedback.firstOrNull() ?: true
            _uiState.value = _uiState.value.copy(hapticFeedbackEnabled = hapticEnabled)
        }
    }

    private fun observeApprovals() {
        viewModelScope.launch {
            regulatorManager.approvalEvents.collect {
                if (_uiState.value.isWaitingForApproval) {
                    _uiState.value = _uiState.value.copy(
                        isWaitingForApproval = false,
                        isFinished = true,
                        approvalMessage = "Regulator approved! Session ended."
                    )
                    restoreDnd()
                }
            }
        }
    }

    private fun loadTask() {
        viewModelScope.launch {
            val tasks = repository.tasks.firstOrNull() ?: emptyList()
            val task = tasks.find { it.id == taskId }
            if (task != null) {
                _uiState.value = _uiState.value.copy(
                    taskName = task.title,
                    durationMin = task.durationMin,
                    remainingSeconds = task.durationMin * 60
                )
                startTimer()
            } else {
                // Handle task not found, maybe fallback
                _uiState.value = _uiState.value.copy(
                    taskName = "Focus Session",
                    durationMin = 25,
                    remainingSeconds = 25 * 60
                )
                startTimer()
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val shouldBlock = preferencesRepository.blockNotifications.firstOrNull() ?: false
            if (shouldBlock) {
                activateDnd()
            }
            while (true) {
                delay(1000)
                val currentState = _uiState.value
                if (!currentState.isPaused && currentState.remainingSeconds > 0) {
                    _uiState.value = currentState.copy(remainingSeconds = currentState.remainingSeconds - 1)
                } else if (currentState.remainingSeconds <= 0) {
                    completeTask()
                    triggerTimerCompleteHaptic()
                    _uiState.value = currentState.copy(isFinished = true)
                    restoreDnd()
                    break
                }
            }
        }
    }

    private suspend fun completeTask() {
        val tasks = repository.tasks.firstOrNull()?.toMutableList() ?: return
        val index = tasks.indexOfFirst { it.id == taskId }
        if (index != -1) {
            tasks[index] = tasks[index].copy(isCompleted = true)
            repository.saveTasks(tasks)
        }
    }

    private fun triggerTimerCompleteHaptic() {
        if (_uiState.value.hapticFeedbackEnabled) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? android.os.Vibrator
            if (vibrator != null && vibrator.hasVibrator()) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    vibrator.vibrate(android.os.VibrationEffect.createOneShot(500, android.os.VibrationEffect.DEFAULT_AMPLITUDE))
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(1000)
                }
            }
        }
    }

    fun togglePause() {
        val current = _uiState.value
        _uiState.value = current.copy(isPaused = !current.isPaused)
    }

    fun endSessionEarly() {
        viewModelScope.launch {
            val totalSeconds = _uiState.value.durationMin * 60
            val elapsedSeconds = totalSeconds - _uiState.value.remainingSeconds
            val percentageCompleted = if (totalSeconds > 0) {
                (elapsedSeconds.toFloat() / totalSeconds.toFloat()) * 100f
            } else {
                0f
            }

            if (percentageCompleted >= 70f) {
                completeTask()
            }

            _uiState.value = _uiState.value.copy(
                isFinished = true,
                navigateToDashboard = true
            )
            timerJob?.cancel()
            restoreDnd()
        }
    }

    fun sendExitRequest() {
        viewModelScope.launch {
            val phone = regulatorRepository.guardianPhone.firstOrNull()
            if (phone.isNullOrBlank()) {
                _uiState.value = _uiState.value.copy(
                    approvalMessage = "No regulator configured. You cannot exit early!"
                )
                return@launch
            }

            _uiState.value = _uiState.value.copy(
                isWaitingForApproval = true,
                approvalMessage = "Request sent. Waiting for SMS approval..."
            )

            val message = "I need to end my focus session early. Please reply APPROVE to allow this."
            val data = Data.Builder()
                .putString(SendSmsWorker.KEY_PHONE_NUMBER, phone)
                .putString(SendSmsWorker.KEY_MESSAGE, message)
                .build()

            val workRequest = OneTimeWorkRequestBuilder<SendSmsWorker>()
                .setInputData(data)
                .build()

            WorkManager.getInstance(context).enqueue(workRequest)
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restoreDnd()
    }

    private fun activateDnd() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            originalInterruptionFilter = notificationManager.currentInterruptionFilter
            val policy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_CALLS,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY
            )
            notificationManager.setNotificationPolicy(policy)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
            dndActivated = true
        }
    }

    private fun restoreDnd() {
        if (dndActivated) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(originalInterruptionFilter)
            }
            dndActivated = false
        }
    }
}
