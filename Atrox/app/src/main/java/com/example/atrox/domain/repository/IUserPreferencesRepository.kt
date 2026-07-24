package com.example.atrox.domain.repository

import kotlinx.coroutines.flow.Flow

interface IUserPreferencesRepository {
    val isLoggedIn: Flow<Boolean>
    val primaryGoal: Flow<String>
    val targetHours: Flow<Float>
    val sprintDuration: Flow<Int>
    val breakDuration: Flow<Int>
    val dailySprints: Flow<Int>
    val maxStreak: Flow<Int>
    val focusGoals: Flow<Set<String>>
    val displayName: Flow<String?>
    val avatarId: Flow<String?>
    val autoStartNextSprint: Flow<Boolean>
    val blockNotifications: Flow<Boolean>
    val isPhoneBlockActive: Flow<Boolean>
    val strictBreakTime: Flow<Boolean>
    val approvalForEarlyExit: Flow<Boolean>
    val sprintReminders: Flow<Boolean>
    val dailyGoalNudge: Flow<Boolean>
    val hapticFeedback: Flow<Boolean>

    suspend fun setLoggedIn(isLoggedIn: Boolean)
    suspend fun setPrimaryGoal(goal: String)
    suspend fun setFocusGoals(goals: Set<String>)
    suspend fun setTargetHours(hours: Float)
    suspend fun setSprintDuration(duration: Int)
    suspend fun setBreakDuration(breakDuration: Int)
    suspend fun setSprintGoal(sprints: Int)
    suspend fun setMaxStreak(streak: Int)
    suspend fun setDisplayName(name: String)
    suspend fun setAvatarId(id: String)
    suspend fun setAutoStartNextSprint(autoStart: Boolean)
    suspend fun setBlockNotifications(block: Boolean)
    suspend fun setPhoneBlockActive(active: Boolean)
    suspend fun setStrictBreakTime(enabled: Boolean)
    suspend fun setApprovalForEarlyExit(approval: Boolean)
    suspend fun setSprintReminders(enabled: Boolean)
    suspend fun setDailyGoalNudge(enabled: Boolean)
    suspend fun setHapticFeedback(enabled: Boolean)
}
