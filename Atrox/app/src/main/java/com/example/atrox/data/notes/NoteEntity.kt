package com.example.atrox.data.notes

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "notes")
data class NoteEntity(
    @PrimaryKey
    val id: String,
    val title: String,
    val content: String,
    val timestamp: Long,
    val hasAudio: Boolean,
    val isSpanning: Boolean,
//    val category: NoteCategory,
    val attachedImages: String, // comma separated list of internal paths
    @ColumnInfo(defaultValue = "0")
    val isPinned: Boolean = false
)
