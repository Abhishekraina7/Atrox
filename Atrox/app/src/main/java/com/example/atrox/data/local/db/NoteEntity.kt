package com.example.atrox.data.local.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.atrox.domain.model.NoteCategory

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String = "",
    val title: String = "",
    val content: String = "",
    val timestamp: Long = 0L,
    val hasAudio: Boolean = false,
    val isSpanning: Boolean = false,
    val category: NoteCategory = NoteCategory.PERSONAL,
    val attachedImages: String = "", // comma separated list of internal paths
    @ColumnInfo(defaultValue = "0")
    val isPinned: Boolean = false,
    @ColumnInfo(defaultValue = "0")
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null,
    
    // Cloud Sync fields
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = 0,
    @ColumnInfo(defaultValue = "0")
    val isSynced: Boolean = false,
    @ColumnInfo(defaultValue = "''")
    val userId: String = ""
)
