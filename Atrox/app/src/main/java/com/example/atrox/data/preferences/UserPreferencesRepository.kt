package com.example.atrox.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {

    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val PRIMARY_GOAL = stringPreferencesKey("primary_goal")
        val TARGET_HOURS = floatPreferencesKey("target_hours")
    }

    val isLoggedIn: Flow<Boolean> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] ?: false
        }
        
    val primaryGoal: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.PRIMARY_GOAL] ?: "Deep Work" }

    val targetHours: Flow<Float> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.TARGET_HOURS] ?: 4f }

    suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }

    suspend fun setPrimaryGoal(goal: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIMARY_GOAL] = goal
        }
    }

    suspend fun setTargetHours(hours: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_HOURS] = hours
        }
    }
}
