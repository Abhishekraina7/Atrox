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
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject
import android.content.Context
import android.app.NotificationManager
import dagger.hilt.android.qualifiers.ApplicationContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

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
    // Today's task rows mapped directly from persistent storage
    val tasks: StateFlow<List<TaskItem>> = taskRepository.tasks.map { list ->
        val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
        val todaysTasks = list.filter { it.dateString == todayStr }
        val (completed, pending) = todaysTasks.partition { it.isCompleted }
        pending + completed
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // Picks the first pending (not completed) task from today's tasks
    val nextPendingTask: StateFlow<TaskItem?> = taskRepository.tasks
        .map { list -> 
            val todayStr = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            list.firstOrNull { !it.isCompleted && it.dateString == todayStr } 
        }
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
        viewModelScope.launch {
            val list = taskRepository.tasks.firstOrNull() ?: return@launch
            val task = list.find { it.id == taskId }
            if (task != null) {
                taskRepository.updateTask(task.copy(isCompleted = !task.isCompleted))
            }
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
