package com.example.atrox.ui.main.dashboard

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class TaskItem(
    val id: String,
    val title: String,
    val category: String,
    val durationMin: Int,
    val isCompleted: Boolean
)

@HiltViewModel
class DashboardViewModel @Inject constructor() : ViewModel() {

    private val _isPhoneBlockActive = MutableStateFlow(true)
    val isPhoneBlockActive = _isPhoneBlockActive.asStateFlow()

    private val _tasks = MutableStateFlow(
        listOf(
            TaskItem("1", "Email Campaign Review", "WORK", 25, false),
            TaskItem("2", "UI Component Library Update", "DESIGN", 45, false),
            TaskItem("3", "Check Slack Messages", "ADMIN", 10, true)
        )
    )
    val tasks = _tasks.asStateFlow()

    fun togglePhoneBlock() {
        _isPhoneBlockActive.value = !_isPhoneBlockActive.value
    }

    fun toggleTaskCompletion(taskId: String) {
        _tasks.value = _tasks.value.map {
            if (it.id == taskId) it.copy(isCompleted = !it.isCompleted) else it
        }
    }
}
