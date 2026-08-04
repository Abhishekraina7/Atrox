package com.example.atrox.data.repository

import com.example.atrox.data.local.db.NoteDao
import com.example.atrox.data.local.db.NoteEntity
import com.example.atrox.domain.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val firebaseAuth: Lazy<FirebaseAuth>
) : INoteRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    override fun getDeletedNotes(): Flow<List<NoteEntity>> = noteDao.getDeletedNotes()

    override fun searchNotesByTitle(query: String): Flow<List<NoteEntity>> = noteDao.searchNotesByTitle(query)

    override fun getNoteById(id: String): Flow<NoteEntity?> = noteDao.getNoteById(id)

    override suspend fun insertNote(note: NoteEntity) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedNote = note.copy(
            userId = uid,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        noteDao.insertNote(updatedNote)
    }

    override suspend fun moveToTrash(id: String, timestamp: Long) {
        noteDao.moveToTrash(id, timestamp)
    }

    override suspend fun restoreNote(id: String) {
        noteDao.restoreNote(id, System.currentTimeMillis())
    }

    override suspend fun permanentlyDeleteNoteById(id: String) {
        noteDao.permanentlyDeleteNoteById(id)
    }

    override suspend fun deleteExpiredNotes(expirationTimestamp: Long) {
        noteDao.deleteExpiredNotes(expirationTimestamp)
    }
}
