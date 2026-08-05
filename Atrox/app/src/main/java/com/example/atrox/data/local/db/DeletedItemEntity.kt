package com.example.atrox.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "deleted_items")
data class DeletedItemEntity(
    @PrimaryKey
    val itemId: String,
    val itemType: String, // "NOTE" or "TASK"
    val userId: String,
    val timestamp: Long = System.currentTimeMillis()
)
