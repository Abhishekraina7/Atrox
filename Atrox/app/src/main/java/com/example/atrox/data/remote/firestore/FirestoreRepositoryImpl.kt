package com.example.atrox.data.remote.firestore

import com.example.atrox.data.local.db.NoteEntity
import com.example.atrox.data.local.db.PreferenceEntity
import com.example.atrox.data.local.db.TaskItem
import com.example.atrox.domain.repository.IFirestoreRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import javax.inject.Inject

class FirestoreRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : IFirestoreRepository {

    override suspend fun syncNotes(userId: String, notes: List<NoteEntity>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            notes.forEach { note ->
                val docRef = firestore.collection("users").document(userId).collection("notes").document(note.id)
                batch.set(docRef, note)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchNotes(userId: String): Result<List<NoteEntity>> {
        return try {
            val snapshot = firestore.collection("users").document(userId).collection("notes").get().await()
            val notes = snapshot.toObjects(NoteEntity::class.java)
            Result.success(notes)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncTasks(userId: String, tasks: List<TaskItem>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            tasks.forEach { task ->
                val docRef = firestore.collection("users").document(userId).collection("tasks").document(task.id)
                batch.set(docRef, task)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchTasks(userId: String): Result<List<TaskItem>> {
        return try {
            val snapshot = firestore.collection("users").document(userId).collection("tasks").get().await()
            val tasks = snapshot.toObjects(TaskItem::class.java)
            Result.success(tasks)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteNote(userId: String, noteId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("notes").document(noteId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteTask(userId: String, taskId: String): Result<Unit> {
        return try {
            firestore.collection("users").document(userId)
                .collection("tasks").document(taskId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun syncPreferences(userId: String, preferences: List<PreferenceEntity>): Result<Unit> {
        return try {
            val batch = firestore.batch()
            preferences.forEach { pref ->
                val docRef = firestore.collection("users").document(userId).collection("preferences").document(pref.key)
                batch.set(docRef, pref)
            }
            batch.commit().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun fetchPreferences(userId: String): Result<List<PreferenceEntity>> {
        return try {
            val snapshot = firestore.collection("users").document(userId).collection("preferences").get().await()
            val preferences = snapshot.toObjects(PreferenceEntity::class.java)
            Result.success(preferences)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
