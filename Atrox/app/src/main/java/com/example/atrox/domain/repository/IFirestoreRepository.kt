package com.example.atrox.domain.repository

import com.example.atrox.data.local.db.NoteEntity
import com.example.atrox.data.local.db.TaskItem

interface IFirestoreRepository {
    suspend fun syncNotes(userId: String, notes: List<NoteEntity>): Result<Unit>
    suspend fun fetchNotes(userId: String): Result<List<NoteEntity>>
    
    suspend fun syncTasks(userId: String, tasks: List<TaskItem>): Result<Unit>
    suspend fun fetchTasks(userId: String): Result<List<TaskItem>>

    suspend fun deleteNote(userId: String, noteId: String): Result<Unit>
    suspend fun deleteTask(userId: String, taskId: String): Result<Unit>
}
