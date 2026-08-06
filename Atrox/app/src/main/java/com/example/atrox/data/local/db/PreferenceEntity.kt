package com.example.atrox.data.local.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "preferences")
data class PreferenceEntity(
    @PrimaryKey
    val key: String = "",
    val value: String = "",
    val valueType: String = "",
    val userId: String = "",
    val updatedAt: Long = 0L,
    
    @get:com.google.firebase.firestore.PropertyName("isSynced")
    @set:com.google.firebase.firestore.PropertyName("isSynced")
    var isSynced: Boolean = false
)
