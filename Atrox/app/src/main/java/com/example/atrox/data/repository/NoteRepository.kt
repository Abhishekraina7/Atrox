package com.example.atrox.data.repository

import com.example.atrox.data.local.db.NoteDao
import com.example.atrox.data.local.db.NoteEntity
import com.example.atrox.domain.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao
) : INoteRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()

    override fun searchNotesByTitle(query: String): Flow<List<NoteEntity>> = noteDao.searchNotesByTitle(query)

    override fun getNoteById(id: String): Flow<NoteEntity?> = noteDao.getNoteById(id)

    override suspend fun insertNote(note: NoteEntity) {
        noteDao.insertNote(note)
    }

    override suspend fun deleteNoteById(id: String) {
        noteDao.deleteNoteById(id)
    }
}
