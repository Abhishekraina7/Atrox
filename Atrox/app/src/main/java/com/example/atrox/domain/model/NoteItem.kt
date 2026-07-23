package com.example.atrox.domain.model

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val rawTimestamp: Long,
    val hasAudio: Boolean = false,
    val isSpanning: Boolean = false,
    val category: NoteCategory = NoteCategory.PERSONAL,
    val isPinned: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedTimestamp: Long? = null
)
