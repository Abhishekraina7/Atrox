package com.example.atrox.data.repository

import com.example.atrox.data.local.db.NoteDao
import com.example.atrox.data.local.db.NoteEntity
import com.example.atrox.domain.repository.INoteRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atrox.domain.sync.CloudSyncManager
import com.example.atrox.worker.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext

class NoteRepository @Inject constructor(
    private val noteDao: NoteDao,
    private val firebaseAuth: Lazy<FirebaseAuth>,
    @ApplicationContext private val context: Context,
    private val cloudSyncManager: CloudSyncManager
) : INoteRepository {
    override fun getAllNotes(): Flow<List<NoteEntity>> = noteDao.getAllNotes()
    
    override fun getDeletedNotes(): Flow<List<NoteEntity>> = noteDao.getDeletedNotes()

    override fun searchNotesByTitle(query: String): Flow<List<NoteEntity>> = noteDao.searchNotesByTitle(query)

    override fun getNoteById(id: String): Flow<NoteEntity?> = noteDao.getNoteById(id)

    private fun triggerSync() {
        cloudSyncManager.syncPushOnlyAsync()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    override suspend fun insertNote(note: NoteEntity) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val updatedNote = note.copy(
            userId = uid,
            updatedAt = System.currentTimeMillis(),
            isSynced = false
        )
        noteDao.insertNote(updatedNote)
        triggerSync()
    }

    override suspend fun moveToTrash(id: String, timestamp: Long) {
        noteDao.moveToTrash(id, timestamp)
        triggerSync()
    }

    override suspend fun restoreNote(id: String) {
        noteDao.restoreNote(id, System.currentTimeMillis())
        triggerSync()
    }

    override suspend fun permanentlyDeleteNoteById(id: String) {
        noteDao.permanentlyDeleteNoteById(id)
    }

    override suspend fun deleteExpiredNotes(expirationTimestamp: Long) {
        noteDao.deleteExpiredNotes(expirationTimestamp)
    }
}
