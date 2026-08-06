package com.example.atrox.data.local.preferences

import android.content.Context
import androidx.work.Constraints
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.atrox.data.local.db.PreferenceDao
import com.example.atrox.data.local.db.PreferenceEntity
import com.example.atrox.domain.repository.IUserPreferencesRepository
import com.example.atrox.domain.sync.CloudSyncManager
import com.example.atrox.worker.SyncWorker
import com.google.firebase.auth.FirebaseAuth
import dagger.Lazy
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val preferenceDao: PreferenceDao,
    private val firebaseAuth: Lazy<FirebaseAuth>,
    @ApplicationContext private val context: Context,
    private val cloudSyncManager: CloudSyncManager
) : IUserPreferencesRepository {

    private fun <T> getPrefFlow(key: String, defaultValue: T, parse: (String) -> T): Flow<T> {
        return preferenceDao.getPreferenceFlow(key).map { entity ->
            if (entity != null) parse(entity.value) else defaultValue
        }
    }

    override val isLoggedIn: Flow<Boolean> = getPrefFlow("is_logged_in", false) { it.toBooleanStrictOrNull() ?: false }
    override val primaryGoal: Flow<String> = getPrefFlow("primary_goal", "Deep Work") { it }
    override val targetHours: Flow<Float> = getPrefFlow("target_hours", 2f) { it.toFloatOrNull() ?: 2f }
    override val sprintDuration: Flow<Int> = getPrefFlow("sprint_duration", 25) { it.toIntOrNull() ?: 25 }
    override val breakDuration: Flow<Int> = getPrefFlow("break_duration", 10) { it.toIntOrNull() ?: 10 }
    override val dailySprints: Flow<Int> = getPrefFlow("daily_sprints_goal", 2) { it.toIntOrNull() ?: 2 }
    override val maxStreak: Flow<Int> = getPrefFlow("streak", 0) { it.toIntOrNull() ?: 0 }
    override val focusGoals: Flow<Set<String>> = getPrefFlow("focus_goals", emptySet()) { if(it.isEmpty()) emptySet() else it.split(",").toSet() }
    override val displayName: Flow<String?> = getPrefFlow("display_name", null) { it }
    override val avatarId: Flow<String?> = getPrefFlow("avatar_id", null) { it }
    override val autoStartNextSprint: Flow<Boolean> = getPrefFlow("auto_start_next_sprint", true) { it.toBooleanStrictOrNull() ?: true }
    override val blockNotifications: Flow<Boolean> = getPrefFlow("block_notifications", true) { it.toBooleanStrictOrNull() ?: true }
    override val isPhoneBlockActive: Flow<Boolean> = getPrefFlow("phone_block_active", false) { it.toBooleanStrictOrNull() ?: false }
    override val strictBreakTime: Flow<Boolean> = getPrefFlow("strict_break_time", false) { it.toBooleanStrictOrNull() ?: false }
    override val approvalForEarlyExit: Flow<Boolean> = getPrefFlow("approval_for_early_exit", false) { it.toBooleanStrictOrNull() ?: false }
    override val sprintReminders: Flow<Boolean> = getPrefFlow("sprint_reminders", true) { it.toBooleanStrictOrNull() ?: true }
    override val dailyGoalNudge: Flow<Boolean> = getPrefFlow("daily_goal_nudge", true) { it.toBooleanStrictOrNull() ?: true }
    override val hapticFeedback: Flow<Boolean> = getPrefFlow("haptic_feedback", true) { it.toBooleanStrictOrNull() ?: true }
    override val unlockedBadges: Flow<Set<String>> = getPrefFlow("unlocked_badges", emptySet()) { if(it.isEmpty()) emptySet() else it.split(",").toSet() }

    private fun triggerSync() {
        cloudSyncManager.syncPushOnlyAsync()
        val constraints = Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build()
        val workRequest = OneTimeWorkRequestBuilder<SyncWorker>().setConstraints(constraints).build()
        WorkManager.getInstance(context).enqueue(workRequest)
    }

    private suspend fun <T> setPref(key: String, value: T, type: String) {
        val uid = firebaseAuth.get().currentUser?.uid ?: ""
        val stringValue = if (value is Set<*>) value.joinToString(",") else value.toString()
        preferenceDao.insertPreference(
            PreferenceEntity(key, stringValue, type, uid, System.currentTimeMillis(), false)
        )
        if (uid.isNotEmpty()) {
            triggerSync()
        }
    }

    override suspend fun setLoggedIn(isLoggedIn: Boolean) = setPref("is_logged_in", isLoggedIn, "BOOLEAN")
    override suspend fun setPrimaryGoal(goal: String) = setPref("primary_goal", goal, "STRING")
    override suspend fun setFocusGoals(goals: Set<String>) = setPref("focus_goals", goals, "STRING_SET")
    override suspend fun setTargetHours(hours: Float) = setPref("target_hours", hours, "FLOAT")
    override suspend fun setSprintDuration(duration: Int) = setPref("sprint_duration", duration, "INT")
    override suspend fun setBreakDuration(breakDuration: Int) = setPref("break_duration", breakDuration, "INT")
    override suspend fun setSprintGoal(sprints: Int) = setPref("daily_sprints_goal", sprints, "INT")
    override suspend fun setMaxStreak(streak: Int) = setPref("streak", streak, "INT")
    override suspend fun setDisplayName(name: String) = setPref("display_name", name, "STRING")
    override suspend fun setAvatarId(id: String) = setPref("avatar_id", id, "STRING")
    override suspend fun setAutoStartNextSprint(autoStart: Boolean) = setPref("auto_start_next_sprint", autoStart, "BOOLEAN")
    override suspend fun setBlockNotifications(block: Boolean) = setPref("block_notifications", block, "BOOLEAN")
    override suspend fun setPhoneBlockActive(active: Boolean) = setPref("phone_block_active", active, "BOOLEAN")
    override suspend fun setStrictBreakTime(enabled: Boolean) = setPref("strict_break_time", enabled, "BOOLEAN")
    override suspend fun setApprovalForEarlyExit(approval: Boolean) = setPref("approval_for_early_exit", approval, "BOOLEAN")
    override suspend fun setSprintReminders(enabled: Boolean) = setPref("sprint_reminders", enabled, "BOOLEAN")
    override suspend fun setDailyGoalNudge(enabled: Boolean) = setPref("daily_goal_nudge", enabled, "BOOLEAN")
    override suspend fun setHapticFeedback(enabled: Boolean) = setPref("haptic_feedback", enabled, "BOOLEAN")

    override suspend fun addUnlockedBadge(badgeId: String) {
        val current = preferenceDao.getPreferenceSync("unlocked_badges")?.value?.split(",")?.toSet() ?: emptySet()
        setPref("unlocked_badges", current + badgeId, "STRING_SET")
    }
}
