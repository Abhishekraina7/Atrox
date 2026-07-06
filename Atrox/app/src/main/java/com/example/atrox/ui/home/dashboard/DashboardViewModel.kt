package com.example.atrox.ui.main.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.data.tasks.TaskItem
import com.example.atrox.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.app.NotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext

@HiltViewModel
class DashboardViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationContext private val context: Context
) : ViewModel() {

    val isPhoneBlockActive = userPreferencesRepository.isPhoneBlockActive.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = false
    )

    init {
        viewModelScope.launch {
            userPreferencesRepository.isPhoneBlockActive.collect { isActive ->
                if (isActive) {
                    activateDnd()
                } else {
                    restoreDnd()
                }
            }
        }
    }

    val maxStreak = userPreferencesRepository.maxStreak.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(),
        initialValue = 0
    )
    // Today's task rows (still in-memory for the dashboard list section)
    private val _tasks = MutableStateFlow(
        listOf(
            TaskItem("1", "Email Campaign Review", "WORK", 25, false),
            TaskItem("2", "UI Component Library Update", "DESIGN", 45, false),
            TaskItem("3", "Check Slack Messages", "ADMIN", 10, true)
        )
    )
    val tasks = _tasks.asStateFlow()

    // Picks the first pending (not completed) task from the persistent TaskRepository
    val nextPendingTask: kotlinx.coroutines.flow.StateFlow<TaskItem?> = taskRepository.tasks
        .map { list -> list.firstOrNull { !it.isCompleted } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = null
        )

    fun incrementStreak(){
        viewModelScope.launch {
            val currentStreak = maxStreak.value
            userPreferencesRepository.setMaxStreak(currentStreak + 1)

        }
    }

    fun togglePhoneBlock() {
        viewModelScope.launch {
            userPreferencesRepository.setPhoneBlockActive(!isPhoneBlockActive.value)
        }
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
    }

    private fun activateDnd() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            val policy = NotificationManager.Policy(
                NotificationManager.Policy.PRIORITY_CATEGORY_CALLS,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY,
                NotificationManager.Policy.PRIORITY_SENDERS_ANY
            )
            notificationManager.setNotificationPolicy(policy)
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_PRIORITY)
        }
    }

    private fun restoreDnd() {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (notificationManager.isNotificationPolicyAccessGranted) {
            notificationManager.setInterruptionFilter(NotificationManager.INTERRUPTION_FILTER_ALL)
        }
    }
}
