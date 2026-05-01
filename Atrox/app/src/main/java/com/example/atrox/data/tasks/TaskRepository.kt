package com.example.atrox.data.tasks

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TaskRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val TASKS_KEY = stringPreferencesKey("pending_tasks")

    val tasks: Flow<List<TaskItem>> = dataStore.data.map { preferences ->
        val json = preferences[TASKS_KEY] ?: return@map emptyList()
        try {
            Json.decodeFromString<List<TaskItem>>(json)
        } catch (e: Exception) {
            emptyList()
        }
    }

    suspend fun saveTasks(tasks: List<TaskItem>) {
        dataStore.edit { preferences ->
            preferences[TASKS_KEY] = Json.encodeToString(tasks)
        }
    }
}
