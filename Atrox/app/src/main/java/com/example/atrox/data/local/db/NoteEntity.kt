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
    @get:com.google.firebase.firestore.PropertyName("isSpanning")
    @set:com.google.firebase.firestore.PropertyName("isSpanning")
    var isSpanning: Boolean = false,
    
    val category: NoteCategory = NoteCategory.PERSONAL,
    val attachedImages: String = "", // comma separated list of internal paths
    
    @get:com.google.firebase.firestore.PropertyName("isPinned")
    @set:com.google.firebase.firestore.PropertyName("isPinned")
    @ColumnInfo(defaultValue = "0")
    var isPinned: Boolean = false,
    
    @get:com.google.firebase.firestore.PropertyName("isDeleted")
    @set:com.google.firebase.firestore.PropertyName("isDeleted")
    @ColumnInfo(defaultValue = "0")
    var isDeleted: Boolean = false,
    
    val deletedTimestamp: Long? = null,
    
    // Cloud Sync fields
    @ColumnInfo(defaultValue = "0")
    val updatedAt: Long = 0,
    
    @get:com.google.firebase.firestore.PropertyName("isSynced")
    @set:com.google.firebase.firestore.PropertyName("isSynced")
    @ColumnInfo(defaultValue = "0")
    var isSynced: Boolean = false,
    
    @ColumnInfo(defaultValue = "''")
    val userId: String = ""
)
