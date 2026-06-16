package com.example.atrox.data.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.preferencesOf
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
        val SPRINT_DURATION = intPreferencesKey("sprint_duration")
        val BREAK_DURATION = intPreferencesKey("break_duration")
        val DAILY_SPRINTS = intPreferencesKey("daily_sprints_goal")
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
        .map { preferences -> preferences[PreferencesKeys.TARGET_HOURS] ?: 2f }

    val sprintDuration: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.SPRINT_DURATION] ?: 25 }

    val breakDuration: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.BREAK_DURATION] ?: 10 }

    val dailySprints: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.DAILY_SPRINTS] ?: 2 }

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

    suspend fun setSprintDuration(duration: Int){
        dataStore.edit { preferences -> preferences[PreferencesKeys.SPRINT_DURATION] = duration
        }
    }

    suspend fun setBreakDuration(breakDuration: Int){
        dataStore.edit { preferences -> preferences[PreferencesKeys.BREAK_DURATION] = breakDuration
        }
    }

    suspend fun setSprintGoal(sprints: Int){
        dataStore.edit { preferences -> preferences[PreferencesKeys.DAILY_SPRINTS] = sprints
        }
    }
}
