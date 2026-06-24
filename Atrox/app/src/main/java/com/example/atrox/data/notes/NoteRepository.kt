package com.example.atrox.data.notes

import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) {
    fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    fun searchNotesByTitle(query: String): Flow<List<NoteEntity>> = noteDao.searchNotesByTitle(query)

    fun getNoteById(id: String): Flow<NoteEntity?> = noteDao.getNoteById(id)

    suspend fun insertNote(note: NoteEntity) {
        noteDao.insertNote(note)
    }

    suspend fun deleteNoteById(id: String) {
        noteDao.deleteNoteById(id)
    }
}