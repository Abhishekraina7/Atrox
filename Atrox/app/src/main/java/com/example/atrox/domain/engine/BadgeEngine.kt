package com.example.atrox.domain.engine

import com.example.atrox.data.local.db.TaskItem
import com.example.atrox.data.local.preferences.UserPreferencesRepository
import com.example.atrox.data.repository.TaskRepository
import com.example.atrox.domain.model.AppBadge
import com.example.atrox.domain.model.BadgeCatalogue
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BadgeEngine @Inject constructor(
    private val taskRepository: TaskRepository,
    private val userPreferencesRepository: UserPreferencesRepository
) {
    private val scope = CoroutineScope(Dispatchers.IO)
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

    // Emits newly unlocked badges to be caught by the UI
    private val _newlyUnlockedBadgeEvent = MutableSharedFlow<AppBadge>(extraBufferCapacity = 5)
    val newlyUnlockedBadgeEvent = _newlyUnlockedBadgeEvent.asSharedFlow()

    init {
        scope.launch {
            combine(
                taskRepository.tasks,
                userPreferencesRepository.unlockedBadges,
                userPreferencesRepository.dailySprints
            ) { tasks, unlockedBadges, dailyGoal ->
                evaluateBadges(tasks.filter { it.isCompleted }, unlockedBadges, dailyGoal)
            }.collect()
        }
    }

    private suspend fun evaluateBadges(
        completedTasks: List<TaskItem>,
        unlockedBadgeIds: Set<String>,
        dailyGoal: Int
    ) {
        if (completedTasks.isEmpty()) return

        val totalSprints = completedTasks.size
        val totalMinutes = completedTasks.sumOf { it.durationMin }
        val totalHours = totalMinutes / 60.0

        // Calculate generic streak
        val completedDates = completedTasks.map { it.dateString }.toSet()
        val currentStreak = calculateStreak(completedDates)

        // Calculate goal streak
        val datesMeetingGoal = completedTasks
            .groupBy { it.dateString }
            .filter { it.value.size >= dailyGoal }
            .keys
        val currentGoalStreak = calculateStreak(datesMeetingGoal)

        // Evaluate each badge in the catalogue
        for (badge in BadgeCatalogue.badges) {
            if (badge.id in unlockedBadgeIds) continue // Already unlocked

            val isUnlocked = when (badge.id) {
                "b1" -> totalSprints >= 1
                "b2" -> totalHours >= 2.0
                "b3" -> currentStreak >= 3
                "b4" -> completedTasks.any { isBefore8AM(it) }
                "b5" -> completedTasks.any { isAfter10PM(it) }
                "b6" -> totalHours >= 10.0
                "b7" -> currentStreak >= 7
                "b8" -> hasWeekendWarrior(completedTasks)
                "b9" -> hasSprintsInSingleDay(completedTasks, 10)
                "b10" -> totalSprints >= 100
                "b11" -> currentGoalStreak >= 7
                "b12" -> totalHours >= 50.0
                "b13" -> currentStreak >= 14
                "b14" -> completedTasks.any { it.durationMin >= 120 }
                "b15" -> totalHours >= 100.0
                "b16" -> currentStreak >= 30
                "b17" -> totalSprints >= 500
                "b18" -> currentStreak >= 60
                "b19" -> currentGoalStreak >= 30
                "b20" -> totalHours >= 500.0
                "b21" -> currentStreak >= 100
                "b22" -> totalSprints >= 1000
                "b23" -> currentGoalStreak >= 100
                "b24" -> totalHours >= 1000.0
                "b25" -> currentStreak >= 365
                else -> false
            }

            if (isUnlocked) {
                userPreferencesRepository.addUnlockedBadge(badge.id)
                _newlyUnlockedBadgeEvent.emit(badge)
            }
        }
    }

    private fun calculateStreak(dates: Set<String>): Int {
        if (dates.isEmpty()) return 0
        var streak = 0
        val currentDate = Calendar.getInstance()
        
        val todayStr = dateFormat.format(currentDate.time)
        if (dates.contains(todayStr)) {
            streak++
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(currentDate.time)
            if (dates.contains(yesterdayStr)) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return 0
            }
        }

        while (true) {
            val dateStr = dateFormat.format(currentDate.time)
            if (dates.contains(dateStr)) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }
        return streak
    }

    private fun hasWeekendWarrior(tasks: List<TaskItem>): Boolean {
        // Group by weekend (a weekend could be considered the combination of Saturday and Sunday)
        // For simplicity, just check if there are 5 sprints on Saturdays/Sundays across the dataset 
        // that share the same weekend (week of year).
        val weekendTasks = tasks.filter {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(it.dateString) ?: Date()
            val day = cal.get(Calendar.DAY_OF_WEEK)
            day == Calendar.SATURDAY || day == Calendar.SUNDAY
        }
        val groupedByWeek = weekendTasks.groupBy {
            val cal = Calendar.getInstance()
            cal.time = dateFormat.parse(it.dateString) ?: Date()
            "${cal.get(Calendar.YEAR)}-${cal.get(Calendar.WEEK_OF_YEAR)}"
        }
        return groupedByWeek.values.any { it.size >= 5 }
    }

    private fun hasSprintsInSingleDay(tasks: List<TaskItem>, count: Int): Boolean {
        return tasks.groupBy { it.dateString }.values.any { it.size >= count }
    }

    private fun isBefore8AM(task: TaskItem): Boolean {
        val todayStr = dateFormat.format(Date())
        if (task.dateString == todayStr) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return hour < 8
        }
        return false 
    }

    private fun isAfter10PM(task: TaskItem): Boolean {
        val todayStr = dateFormat.format(Date())
        if (task.dateString == todayStr) {
            val hour = Calendar.getInstance().get(Calendar.HOUR_OF_DAY)
            return hour >= 22
        }
        return false 
    }
}
