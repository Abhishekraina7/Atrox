package com.example.atrox.ui.home.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.local.preferences.UserPreferencesRepository
import com.example.atrox.data.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.app.NotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class FocusBreakUiState(
    val remainingSeconds: Int = 5 * 60, // 5 minutes
    val selectedAtmosphere: String = "Rain",
    val completedSprints: Int = 2,
    val totalSprints: Int = 4,
    val focusMinutes: Int = 50,
    val focusSeconds: Int = 0,
    val dailyProgressPercent: Int = 75,
    val isFinished: Boolean = false,
    val nextTaskId: String? = null,
    val isAutoStartEnabled: Boolean = true,
    val strictBreakTime: Boolean = false
)

@HiltViewModel
class FocusBreakViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val preferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(FocusBreakUiState())
    val uiState: StateFlow<FocusBreakUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var dndActivated = false
    private var isPhoneBlockEnabled = false
    private var originalInterruptionFilter: Int = NotificationManager.INTERRUPTION_FILTER_ALL

    init {
        startTimer()
        viewModelScope.launch {
            preferencesRepository.autoStartNextSprint.collect { autoStart ->
                _uiState.value = _uiState.value.copy(isAutoStartEnabled = autoStart)
            }
        }
        viewModelScope.launch {
            preferencesRepository.strictBreakTime.collect { strict ->
                _uiState.value = _uiState.value.copy(strictBreakTime = strict)
            }
        }
        viewModelScope.launch {
            taskRepository.tasks.collect { tasks ->
                val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val todaysTasks = tasks.filter { it.dateString == todayStr }
                val completed = todaysTasks.count { it.isCompleted }
                val total = todaysTasks.size
                
                val totalFocusMin = todaysTasks.filter { it.isCompleted }.sumOf { it.durationMin }
                val progressPercent = if (total > 0) ((completed.toFloat() / total.toFloat()) * 100).toInt() else 0
                
                _uiState.value = _uiState.value.copy(
                    completedSprints = completed,
                    totalSprints = total,
                    focusMinutes = totalFocusMin,
                    focusSeconds = 0,
                    dailyProgressPercent = progressPercent
                )
            }
        }
    }

    private fun startTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            val isStrict = preferencesRepository.strictBreakTime.firstOrNull() ?: false
            isPhoneBlockEnabled = preferencesRepository.isPhoneBlockActive.firstOrNull() ?: false
            
            if (isStrict && !isPhoneBlockEnabled) {
                activateDnd()
            }
            while (true) {
                delay(1000)
                val current = _uiState.value
                if (current.remainingSeconds > 0) {
                    _uiState.value = current.copy(remainingSeconds = current.remainingSeconds - 1)
                } else {
                    finishBreak()
                    restoreDnd()
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
            restoreDnd()
        }
    }

    override fun onCleared() {
        super.onCleared()
        timerJob?.cancel()
        restoreDnd()
    }

    private fun activateDnd() {
        if (isPhoneBlockEnabled) return
        
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
        if (isPhoneBlockEnabled) return
        
        if (dndActivated) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (notificationManager.isNotificationPolicyAccessGranted) {
                notificationManager.setInterruptionFilter(originalInterruptionFilter)
            }
            dndActivated = false
        }
    }
}
