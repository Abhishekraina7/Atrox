package com.example.atrox.data.tasks

import kotlinx.serialization.Serializable

@Serializable
data class TaskItem(
    val id: String,
    val title: String,
    val category: String,
    val durationMin: Int,
    val isCompleted: Boolean
)
