package com.example.atrox.domain.sync

import com.example.atrox.data.local.db.NoteDao
import com.example.atrox.data.local.db.TaskDao
import com.example.atrox.domain.repository.IFirestoreRepository
import com.google.firebase.auth.FirebaseAuth
import android.util.Log
import dagger.Lazy
import javax.inject.Inject
import javax.inject.Singleton

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

@Singleton
class CloudSyncManager @Inject constructor(
    private val firestoreRepository: IFirestoreRepository,
    private val noteDao: NoteDao,
    private val taskDao: TaskDao,
    private val firebaseAuth: Lazy<FirebaseAuth>
) {
    private val syncScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    fun sync() {
        syncScope.launch {
            val currentUser = firebaseAuth.get().currentUser
            if (currentUser == null) {
                Log.d("CloudSyncManager", "sync: User is null, aborting sync.")
                return@launch
            }
            val userId = currentUser.uid
            Log.d("CloudSyncManager", "sync: Starting sync for user $userId")

        // 1. PULL REMOTE CHANGES
        pullNotes(userId)
        pullTasks(userId)

            // 2. PUSH LOCAL CHANGES
            pushNotes(userId)
            pushTasks(userId)
            
            Log.d("CloudSyncManager", "sync: Finished sync for user $userId")
        }
    }

    private suspend fun pullNotes(userId: String) {
        Log.d("CloudSyncManager", "pullNotes: Fetching remote notes...")
        firestoreRepository.fetchNotes(userId).onSuccess { remoteNotes ->
            Log.d("CloudSyncManager", "pullNotes: Fetched ${remoteNotes.size} remote notes.")
            if (remoteNotes.isEmpty()) return@onSuccess
            
            val localNotes = noteDao.getAllNotesForUser(userId).associateBy { it.id }
            val notesToInsert = mutableListOf<com.example.atrox.data.local.db.NoteEntity>()

            for (remoteNote in remoteNotes) {
                val localNote = localNotes[remoteNote.id]
                // Last write wins: if remote is newer or local doesn't exist, we take remote.
                if (localNote == null || remoteNote.updatedAt > localNote.updatedAt) {
                    notesToInsert.add(remoteNote.copy(isSynced = true))
                }
            }

            if (notesToInsert.isNotEmpty()) {
                Log.d("CloudSyncManager", "pullNotes: Inserting ${notesToInsert.size} notes into local DB.")
                noteDao.insertNotes(notesToInsert)
            } else {
                Log.d("CloudSyncManager", "pullNotes: No remote notes needed to be synced locally.")
            }
        }.onFailure {
            Log.e("CloudSyncManager", "pullNotes: Failed to fetch remote notes", it)
        }
    }

    private suspend fun pushNotes(userId: String) {
        Log.d("CloudSyncManager", "pushNotes: Checking for unsynced local notes...")
        val unsyncedNotes = noteDao.getUnsyncedNotes(userId)
        if (unsyncedNotes.isEmpty()) {
            Log.d("CloudSyncManager", "pushNotes: No unsynced local notes found.")
            return
        }

        Log.d("CloudSyncManager", "pushNotes: Pushing ${unsyncedNotes.size} notes to Firestore...")
        firestoreRepository.syncNotes(userId, unsyncedNotes).onSuccess {
            Log.d("CloudSyncManager", "pushNotes: Successfully pushed notes. Marking as synced locally.")
            val syncedNotes = unsyncedNotes.map { it.copy(isSynced = true) }
            noteDao.insertNotes(syncedNotes)
        }.onFailure {
            Log.e("CloudSyncManager", "pushNotes: Failed to push notes to Firestore", it)
        }
    }

    private suspend fun pullTasks(userId: String) {
        Log.d("CloudSyncManager", "pullTasks: Fetching remote tasks...")
        firestoreRepository.fetchTasks(userId).onSuccess { remoteTasks ->
            Log.d("CloudSyncManager", "pullTasks: Fetched ${remoteTasks.size} remote tasks.")
            if (remoteTasks.isEmpty()) return@onSuccess
            
            val localTasks = taskDao.getAllTasksForUser(userId).associateBy { it.id }
            val tasksToInsert = mutableListOf<com.example.atrox.data.local.db.TaskItem>()

            for (remoteTask in remoteTasks) {
                val localTask = localTasks[remoteTask.id]
                if (localTask == null || remoteTask.updatedAt > localTask.updatedAt) {
                    tasksToInsert.add(remoteTask.copy(isSynced = true))
                }
            }

            if (tasksToInsert.isNotEmpty()) {
                Log.d("CloudSyncManager", "pullTasks: Inserting ${tasksToInsert.size} tasks into local DB.")
                taskDao.insertTasks(tasksToInsert)
            } else {
                Log.d("CloudSyncManager", "pullTasks: No remote tasks needed to be synced locally.")
            }
        }.onFailure {
            Log.e("CloudSyncManager", "pullTasks: Failed to fetch remote tasks", it)
        }
    }

    private suspend fun pushTasks(userId: String) {
        Log.d("CloudSyncManager", "pushTasks: Checking for unsynced local tasks...")
        val unsyncedTasks = taskDao.getUnsyncedTasks(userId)
        if (unsyncedTasks.isEmpty()) {
            Log.d("CloudSyncManager", "pushTasks: No unsynced local tasks found.")
            return
        }

        Log.d("CloudSyncManager", "pushTasks: Pushing ${unsyncedTasks.size} tasks to Firestore...")
        firestoreRepository.syncTasks(userId, unsyncedTasks).onSuccess {
            Log.d("CloudSyncManager", "pushTasks: Successfully pushed tasks. Marking as synced locally.")
            val syncedTasks = unsyncedTasks.map { it.copy(isSynced = true) }
            taskDao.insertTasks(syncedTasks)
        }.onFailure {
            Log.e("CloudSyncManager", "pushTasks: Failed to push tasks to Firestore", it)
        }
    }
}
