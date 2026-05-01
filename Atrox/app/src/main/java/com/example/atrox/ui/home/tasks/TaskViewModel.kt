package com.example.atrox.ui.home.tasks

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.tasks.TaskItem
import com.example.atrox.data.tasks.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class TaskViewModel @Inject constructor(
    private val repository: TaskRepository
) : ViewModel() {

    val tasks: StateFlow<List<TaskItem>> = repository.tasks.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun addTask(title: String, durationMin: Int) {
        viewModelScope.launch {
            val updatedList = tasks.value + TaskItem(
                id = UUID.randomUUID().toString(),
                title = title,
                category = "FOCUS",
                durationMin = durationMin,
                isCompleted = false
            )
            repository.saveTasks(updatedList)
        }
    }

    fun removeTask(taskId: String) {
        viewModelScope.launch {
            val updatedList = tasks.value.filter { it.id != taskId }
            repository.saveTasks(updatedList)
        }
    }
}
