package com.example.atrox.service.notification

import android.os.Build
import androidx.annotation.RequiresApi
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.data.tasks.TaskRepository
import kotlinx.coroutines.flow.firstOrNull
import java.time.LocalDateTime
import java.time.temporal.ChronoUnit
import javax.inject.Inject

sealed class NotificationState {
    data class ShouldSend(
        val scenarioId: Int,
        val title: String,
        val message: String,
        val channelId: String = NotificationHelper.CHANNEL_ID_GOALS
    ) : NotificationState()
    object DoNotSend : NotificationState()
}

class NotificationDecisionEngine @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val taskRepository: TaskRepository
) {
    @RequiresApi(Build.VERSION_CODES.O)
    suspend fun evaluateScenario(currentTime: LocalDateTime = LocalDateTime.now()): NotificationState {
        val sprintRemindersEnabled = preferencesRepository.sprintReminders.firstOrNull() ?: true
        val dailyGoalNudgeEnabled = preferencesRepository.dailyGoalNudge.firstOrNull() ?: true
        
        if (!sprintRemindersEnabled && !dailyGoalNudgeEnabled) {
            return NotificationState.DoNotSend
        }

        val tasks = taskRepository.tasks.firstOrNull() ?: emptyList()
        val dailyGoal = preferencesRepository.dailySprints.firstOrNull() ?: 2
        val completedSprintsToday = tasks.count { it.isCompleted } // We assume they are cleared daily, or we'd filter by today's date

        // Find last completed sprint time (mock logic, ideally tasks should have a completedAt timestamp)
        // For this demo, let's assume we can get hours since last active. Let's default to a safe 5 hours.
        val hoursSinceLastActive = 5L 
        
        val hour = currentTime.hour
        val streak = preferencesRepository.maxStreak.firstOrNull() ?: 0

        // 1. Morning Kickoff (Exact Time: 9:00 AM)
        if (sprintRemindersEnabled && hour == 9 && completedSprintsToday == 0) {
            return NotificationState.ShouldSend(
                scenarioId = 1,
                title = "Ready to crush today?",
                message = "Set your focus goal for the day and complete your first sprint!"
            )
        }

        // 2. Streak Protection (Exact Time: 20:00 / 8:00 PM)
        if (sprintRemindersEnabled && hour == 20 && completedSprintsToday == 0 && streak > 0) {
            return NotificationState.ShouldSend(
                scenarioId = 3,
                title = "⚠️ Don't break the chain!",
                message = "Complete just 1 sprint to save your $streak-day streak."
            )
        }

        // 3. Goal Achieved (Milestone)
        if (completedSprintsToday >= dailyGoal && dailyGoal > 0) {
            // We usually don't want to spam this, so we might need a flag to track if it was sent today.
            // For now, let's skip sending this continuously.
        }

        // 2. Mid-day Slump (Behavioral)
        if (sprintRemindersEnabled && hour in 13..16 && hoursSinceLastActive >= 3) {
            return NotificationState.ShouldSend(
                scenarioId = 6,
                title = "Mid-day Slump?",
                message = "Afternoon slump hitting? A quick 25-minute sprint will reset your focus."
            )
        }

        // 3. Evening Rescue (Time-Based)
        if (dailyGoalNudgeEnabled && hour == 18 && completedSprintsToday < dailyGoal / 2) {
            return NotificationState.ShouldSend(
                scenarioId = 2,
                title = "The day isn't over yet",
                message = "Just a few sprints can save today's progress. You can still do it!"
            )
        }
        
        // 4. Overworking Warning (Behavioral)
        if (completedSprintsToday > dailyGoal * 1.5) {
            return NotificationState.ShouldSend(
                scenarioId = 8,
                title = "You're on fire today!",
                message = "But remember, rest is crucial. Avoid burnout and take a real break."
            )
        }

        return NotificationState.DoNotSend
    }
}
