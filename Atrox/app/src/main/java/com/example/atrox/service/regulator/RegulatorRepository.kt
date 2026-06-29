package com.example.atrox.service.regulator

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegulatorRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    private val GUARDIAN_PHONE_KEY = stringPreferencesKey("guardian_phone")
    private val GUARDIAN_NAME_KEY = stringPreferencesKey("guardian_name")

    val guardianPhone: Flow<String?> = dataStore.data.map { preferences ->
        preferences[GUARDIAN_PHONE_KEY]
    }

    val guardianName: Flow<String?> = dataStore.data.map { preferences ->
        preferences[GUARDIAN_NAME_KEY]
    }

    suspend fun saveGuardianPhone(phone: String) {
        dataStore.edit { preferences ->
            preferences[GUARDIAN_PHONE_KEY] = phone
        }
    }

    suspend fun saveGuardianName(name: String) {
        dataStore.edit { preferences ->
            preferences[GUARDIAN_NAME_KEY] = name
        }
    }

    suspend fun clearGuardian() {
        dataStore.edit { preferences ->
            preferences.remove(GUARDIAN_PHONE_KEY)
            preferences.remove(GUARDIAN_NAME_KEY)
        }
    }
}