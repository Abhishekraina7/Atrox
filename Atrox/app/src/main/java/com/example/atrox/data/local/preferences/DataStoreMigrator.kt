package com.example.atrox.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import com.example.atrox.data.local.db.PreferenceDao
import com.example.atrox.data.local.db.PreferenceEntity
import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class DataStoreMigrator @Inject constructor(
    private val dataStore: DataStore<Preferences>,
    private val preferenceDao: PreferenceDao,
    private val firebaseAuth: Lazy<FirebaseAuth>
) {
    suspend fun migrateIfNeeded() {
        val migrated = preferenceDao.getPreferenceSync("is_datastore_migrated")
        if (migrated != null && migrated.value == "true") {
            return
        }

        val userId = firebaseAuth.get().currentUser?.uid ?: ""
        val preferences = dataStore.data.first()
        val entities = mutableListOf<PreferenceEntity>()
        val now = System.currentTimeMillis()

        preferences.asMap().forEach { (key, value) ->
            val valueType = when (value) {
                is Int -> "INT"
                is Boolean -> "BOOLEAN"
                is Float -> "FLOAT"
                is Long -> "LONG"
                is String -> "STRING"
                is Set<*> -> "STRING_SET"
                else -> "STRING"
            }
            val stringValue = if (value is Set<*>) {
                value.joinToString(",")
            } else {
                value.toString()
            }
            
            entities.add(
                PreferenceEntity(
                    key = key.name,
                    value = stringValue,
                    valueType = valueType,
                    userId = userId,
                    updatedAt = now,
                    isSynced = false
                )
            )
        }

        if (entities.isNotEmpty()) {
            preferenceDao.insertPreferences(entities)
        }
        
        preferenceDao.insertPreference(
            PreferenceEntity(
                key = "is_datastore_migrated",
                value = "true",
                valueType = "BOOLEAN",
                userId = userId,
                updatedAt = now,
                isSynced = false
            )
        )
        
        dataStore.edit { it.clear() }
    }
}
