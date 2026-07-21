package com.example.atrox.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "task_table")
data class TaskItem(
    @PrimaryKey
    val id: String,
    val title: String,
    val category: String,
    val durationMin: Int,
    val isCompleted: Boolean,
    val dateString: String = ""
)
