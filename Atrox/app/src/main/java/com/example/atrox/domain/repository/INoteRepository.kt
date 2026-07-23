package com.example.atrox.domain.repository

import com.example.atrox.data.local.db.NoteEntity
import kotlinx.coroutines.flow.Flow

interface INoteRepository {
    fun getAllNotes(): Flow<List<NoteEntity>>
    fun getDeletedNotes(): Flow<List<NoteEntity>>
    fun searchNotesByTitle(query: String): Flow<List<NoteEntity>>
    fun getNoteById(id: String): Flow<NoteEntity?>
    suspend fun insertNote(note: NoteEntity)
    suspend fun moveToTrash(id: String, timestamp: Long)
    suspend fun restoreNote(id: String)
    suspend fun permanentlyDeleteNoteById(id: String)
    suspend fun deleteExpiredNotes(expirationTimestamp: Long)
}
