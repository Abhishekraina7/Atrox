package com.example.atrox.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey
import kotlinx.serialization.Serializable

@Serializable
@Entity(tableName = "task_table")
data class TaskItem(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val durationMin: Int = 0,
    val isCompleted: Boolean = false,
    val dateString: String = "",
    
    // Cloud Sync fields
    @androidx.room.ColumnInfo(defaultValue = "0")
    val updatedAt: Long = System.currentTimeMillis(),
    @androidx.room.ColumnInfo(defaultValue = "0")
    val isSynced: Boolean = false,
    @androidx.room.ColumnInfo(defaultValue = "''")
    val userId: String = ""
)
