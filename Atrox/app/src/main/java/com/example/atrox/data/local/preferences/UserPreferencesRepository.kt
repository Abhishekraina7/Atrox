package com.example.atrox.data.local.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import com.example.atrox.domain.repository.IUserPreferencesRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import java.io.IOException
import javax.inject.Inject

class UserPreferencesRepository @Inject constructor(
    private val dataStore: DataStore<Preferences>
) : IUserPreferencesRepository {

    private object PreferencesKeys {
        val IS_LOGGED_IN = booleanPreferencesKey("is_logged_in")
        val PRIMARY_GOAL = stringPreferencesKey("primary_goal")
        val TARGET_HOURS = floatPreferencesKey("target_hours")
        val SPRINT_DURATION = intPreferencesKey("sprint_duration")
        val BREAK_DURATION = intPreferencesKey("break_duration")
        val DAILY_SPRINTS = intPreferencesKey("daily_sprints_goal")
        val STREAK_DAYS = intPreferencesKey("streak")
        val FOCUS_GOALS = stringSetPreferencesKey("focus_goals")
        val DISPLAY_NAME = stringPreferencesKey("display_name")
        val AVATAR_ID = stringPreferencesKey("avatar_id")
        val AUTO_START_NEXT_SPRINT = booleanPreferencesKey("auto_start_next_sprint")
        val BLOCK_NOTIFICATIONS = booleanPreferencesKey("block_notifications")
        val PHONE_BLOCK_ACTIVE = booleanPreferencesKey("phone_block_active")
        val STRICT_BREAK_TIME = booleanPreferencesKey("strict_break_time")
        val APPROVAL_FOR_EARLY_EXIT = booleanPreferencesKey("approval_for_early_exit")
        val SPRINT_REMINDERS = booleanPreferencesKey("sprint_reminders")
        val DAILY_GOAL_NUDGE = booleanPreferencesKey("daily_goal_nudge")
        val HAPTIC_FEEDBACK = booleanPreferencesKey("haptic_feedback")
    }

    override val isLoggedIn: Flow<Boolean> = dataStore.data
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
        
    override val primaryGoal: Flow<String> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.PRIMARY_GOAL] ?: "Deep Work" }

    override val targetHours: Flow<Float> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.TARGET_HOURS] ?: 2f }

    override val sprintDuration: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.SPRINT_DURATION] ?: 25 }

    override val breakDuration: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.BREAK_DURATION] ?: 10 }

    override val dailySprints: Flow<Int> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.DAILY_SPRINTS] ?: 2 }

    override val maxStreak: Flow<Int> = dataStore.data
        .catch{emit(emptyPreferences())}
        .map{preferences -> preferences[PreferencesKeys.STREAK_DAYS] ?: 0}

    override val focusGoals: Flow<Set<String>> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.FOCUS_GOALS] ?: emptySet() }

    override val displayName: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.DISPLAY_NAME] }

    override val avatarId: Flow<String?> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.AVATAR_ID] }

    override val autoStartNextSprint: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.AUTO_START_NEXT_SPRINT] ?: true }

    override val blockNotifications: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.BLOCK_NOTIFICATIONS] ?: true }

    override val isPhoneBlockActive: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.PHONE_BLOCK_ACTIVE] ?: false }

    override val strictBreakTime: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.STRICT_BREAK_TIME] ?: false }

    override val approvalForEarlyExit: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.APPROVAL_FOR_EARLY_EXIT] ?: false }

    override val sprintReminders: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.SPRINT_REMINDERS] ?: true }

    override val dailyGoalNudge: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.DAILY_GOAL_NUDGE] ?: true }

    override val hapticFeedback: Flow<Boolean> = dataStore.data
        .catch { emit(emptyPreferences()) }
        .map { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK] ?: true }

    //functions
    override suspend fun setLoggedIn(isLoggedIn: Boolean) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.IS_LOGGED_IN] = isLoggedIn
        }
    }

    override suspend fun setPrimaryGoal(goal: String) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.PRIMARY_GOAL] = goal
        }
    }

    override suspend fun setFocusGoals(goals: Set<String>) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.FOCUS_GOALS] = goals
        }
    }

    override suspend fun setTargetHours(hours: Float) {
        dataStore.edit { preferences ->
            preferences[PreferencesKeys.TARGET_HOURS] = hours
        }
    }

    override suspend fun setSprintDuration(duration: Int){
        dataStore.edit { preferences -> preferences[PreferencesKeys.SPRINT_DURATION] = duration
        }
    }

    override suspend fun setBreakDuration(breakDuration: Int){
        dataStore.edit { preferences -> preferences[PreferencesKeys.BREAK_DURATION] = breakDuration
        }
    }

    override suspend fun setSprintGoal(sprints: Int){
        dataStore.edit { preferences -> preferences[PreferencesKeys.DAILY_SPRINTS] = sprints
        }
    }

    override suspend fun setMaxStreak(streak: Int){
        dataStore.edit{preferences -> preferences[PreferencesKeys.STREAK_DAYS] = streak}
    }

    override suspend fun setDisplayName(name: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DISPLAY_NAME] = name }
    }

    override suspend fun setAvatarId(id: String) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.AVATAR_ID] = id }
    }

    override suspend fun setAutoStartNextSprint(autoStart: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.AUTO_START_NEXT_SPRINT] = autoStart }
    }

    override suspend fun setBlockNotifications(block: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.BLOCK_NOTIFICATIONS] = block }
    }

    override suspend fun setPhoneBlockActive(active: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.PHONE_BLOCK_ACTIVE] = active }
    }

    override suspend fun setStrictBreakTime(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.STRICT_BREAK_TIME] = enabled }
    }

    override suspend fun setApprovalForEarlyExit(approval: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.APPROVAL_FOR_EARLY_EXIT] = approval }
    }

    override suspend fun setSprintReminders(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.SPRINT_REMINDERS] = enabled }
    }

    override suspend fun setDailyGoalNudge(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.DAILY_GOAL_NUDGE] = enabled }
    }

    override suspend fun setHapticFeedback(enabled: Boolean) {
        dataStore.edit { preferences -> preferences[PreferencesKeys.HAPTIC_FEEDBACK] = enabled }
    }
}
