package com.example.atrox.data.repository

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atrox.data.local.db.PreferenceDao
import com.example.atrox.data.local.db.PreferenceEntity
import com.example.atrox.domain.repository.IRegulatorRepository
import com.example.atrox.domain.sync.CloudSyncManager
import com.example.atrox.worker.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegulatorRepository @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val firebaseAuth: Lazy<FirebaseAuth>,
    @ApplicationContext private val context: Context,
    private val cloudSyncManager: CloudSyncManager
) : IRegulatorRepository {

    private fun <T> getPrefFlow(key: String, parse: (String) -> T): Flow<T?> {
        return preferenceDao.getPreferenceFlow(key).map { entity ->
            if (entity != null) parse(entity.value) else null
        }
    }

    override val guardianPhone: Flow<String?> = getPrefFlow("guardian_phone") { it }
    override val guardianName: Flow<String?> = getPrefFlow("guardian_name") { it }
    override val guardianConnectedSince: Flow<Long?> = getPrefFlow("guardian_connected_since") { it.toLongOrNull() ?: 0L }

    private fun triggerSync() {
        cloudSyncManager.syncPushOnlyAsync()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private suspend fun <T> setPref(key: String, value: T, type: String) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        preferenceDao.insertPreference(
            PreferenceEntity(key, value.toString(), type, uid, System.currentTimeMillis(), false)
        )
        if (uid.isNotEmpty()) {
            triggerSync()
        }
    }

    override suspend fun saveGuardianPhone(phone: String) = setPref("guardian_phone", phone, "STRING")
    override suspend fun saveGuardianName(name: String) = setPref("guardian_name", name, "STRING")
    override suspend fun saveGuardianConnectedSince(timestamp: Long) = setPref("guardian_connected_since", timestamp, "LONG")

    override suspend fun clearGuardian() {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val now = System.currentTimeMillis()
        preferenceDao.insertPreferences(listOf(
            PreferenceEntity("guardian_phone", "", "STRING", uid, now, false),
            PreferenceEntity("guardian_name", "", "STRING", uid, now, false),
            PreferenceEntity("guardian_connected_since", "0", "LONG", uid, now, false)
        ))
        if (uid.isNotEmpty()) {
            triggerSync()
        }
    }
}
