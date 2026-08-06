package com.example.atrox.data.local.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface PreferenceDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreference(preference: PreferenceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPreferences(preferences: List<PreferenceEntity>)

    @Query("SELECT * FROM preferences WHERE key = :key LIMIT 1")
    fun getPreferenceFlow(key: String): Flow<PreferenceEntity?>

    @Query("SELECT * FROM preferences WHERE key = :key LIMIT 1")
    suspend fun getPreferenceSync(key: String): PreferenceEntity?

    @Query("SELECT * FROM preferences WHERE userId = :userId AND isSynced = 0")
    suspend fun getUnsyncedPreferences(userId: String): List<PreferenceEntity>

    @Query("SELECT * FROM preferences WHERE userId = :userId")
    suspend fun getAllPreferencesForUser(userId: String): List<PreferenceEntity>

    @Query("UPDATE preferences SET isSynced = 1 WHERE key = :key AND updatedAt = :updatedAt")
    suspend fun markPreferenceAsSynced(key: String, updatedAt: Long)
}
