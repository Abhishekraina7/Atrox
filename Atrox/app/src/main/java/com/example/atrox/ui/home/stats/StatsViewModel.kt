package com.example.atrox.ui.home.stats

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.atrox.domain.model.BadgeCatalogue
import com.example.atrox.domain.model.Avatar
import com.example.atrox.domain.model.AvatarCatalogue
import com.example.atrox.data.local.preferences.UserPreferencesRepository
import com.example.atrox.data.repository.TaskRepository
import com.example.atrox.data.local.db.TaskItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import androidx.lifecycle.viewModelScope
import com.example.atrox.ui.home.profile.BadgeState
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

data class FocusDashboardUiState(
    val avatar: Avatar? = null,
    val avatarInitial: String = "U",
    val weeklyTotalHours: Double = 0.0,
    val weeklyAverageHours: Double = 0.0,
    val weeklyBestHours: Double = 0.0,
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val consistencyScore: Int = 0,
    val unlockedBadges: List<BadgeState> = emptyList(),
    val todayFocusWorkHours: Int = 6,
    val todayFocusWorkMinutes: Int = 12,
    val todayPhoneUseHours: Int = 2,
    val todayPhoneUseMinutes: Int = 24,
    val moreFocusedPercentage: Int = 22,
    val insightText: String = "Afternoon focus sessions are proving to be your peak performance window."
)

@HiltViewModel
class FocusViewModel @Inject constructor(
    private val userPreferencesRepository: UserPreferencesRepository,
    private val taskRepository: TaskRepository,
    private val firebaseAuth: FirebaseAuth
) : ViewModel() {
    private val _uiState = MutableStateFlow(FocusDashboardUiState())
    val uiState = _uiState.asStateFlow()

    private val _selectedDateTasks = MutableStateFlow<List<TaskItem>>(emptyList())
    val selectedDateTasks = _selectedDateTasks.asStateFlow()

    private val _completedTaskDates = MutableStateFlow<Set<String>>(emptySet())
    val completedTaskDates = _completedTaskDates.asStateFlow()

    val chartData: StateFlow<List<List<Float>>> = taskRepository.tasks.map { tasks ->
        val completed = tasks.filter { it.isCompleted && it.dateString.isNotBlank() }
        
        val cal = Calendar.getInstance()
        cal.firstDayOfWeek = Calendar.MONDAY
        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        val currentMonday = cal.clone() as Calendar
        
        val weeksData = mutableListOf<List<Float>>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        
        for (weekOffset in -2..0) {
            val weekMonday = currentMonday.clone() as Calendar
            weekMonday.add(Calendar.WEEK_OF_YEAR, weekOffset)
            
            val weekFloats = mutableListOf<Float>()
            for (dayOffset in 0..6) {
                val dayCal = weekMonday.clone() as Calendar
                dayCal.add(Calendar.DAY_OF_YEAR, dayOffset)
                val dateStr = dateFormat.format(dayCal.time)
                
                val dailyMins = completed.filter { it.dateString == dateStr }.sumOf { it.durationMin }
                val dailyHours = Math.round((dailyMins / 60.0) * 10.0) / 10f
                weekFloats.add(dailyHours)
            }
            weeksData.add(weekFloats)
        }
        weeksData
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        loadBadges()
        loadUserProfile()
        loadCompletedTaskDates()
        loadPerformanceStats()
    }

    private fun loadPerformanceStats() {
        viewModelScope.launch {
            combine(
                taskRepository.tasks,
                userPreferencesRepository.maxStreak
            ) { tasks, maxStreak ->
                val completedTasks = tasks.filter { it.isCompleted && it.dateString.isNotBlank() }
                
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val today = Calendar.getInstance()
                
                val last7Days = (0..6).map { i ->
                    val cal = today.clone() as Calendar
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    dateFormat.format(cal.time)
                }.toSet()
                
                val recentTasks = completedTasks.filter { it.dateString in last7Days }
                val totalMins = recentTasks.sumOf { it.durationMin }
                val totalHours = Math.round((totalMins / 60.0) * 10.0) / 10.0
                val averageHours = Math.round((totalHours / 7.0) * 10.0) / 10.0
                
                val groupedByDate = completedTasks.groupBy { it.dateString }
                val bestMins = groupedByDate.maxOfOrNull { (_, dailyTasks) ->
                    dailyTasks.sumOf { it.durationMin }
                } ?: 0
                val bestHours = Math.round((bestMins / 60.0) * 10.0) / 10.0
                
                val completedDates = groupedByDate.keys
                val currentStreak = calculateStreak(completedDates)
                
                val last30Days = (0..29).map { i ->
                    val cal = today.clone() as Calendar
                    cal.add(Calendar.DAY_OF_YEAR, -i)
                    dateFormat.format(cal.time)
                }.toSet()
                val activeDaysInLast30 = completedDates.count { it in last30Days }
                val consistencyScore = ((activeDaysInLast30 / 30.0) * 100).toInt()
                
                _uiState.value = _uiState.value.copy(
                    weeklyTotalHours = totalHours,
                    weeklyAverageHours = averageHours,
                    weeklyBestHours = bestHours,
                    currentStreak = currentStreak,
                    longestStreak = maxStreak,
                    consistencyScore = consistencyScore
                )
            }.collect { }
        }
    }
    
    private fun calculateStreak(completedDates: Set<String>): Int {
        if (completedDates.isEmpty()) return 0

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val today = Calendar.getInstance()

        var streak = 0
        val currentDate = today.clone() as Calendar

        val todayStr = dateFormat.format(currentDate.time)
        
        if (completedDates.contains(todayStr)) {
            streak++
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
        } else {
            currentDate.add(Calendar.DAY_OF_YEAR, -1)
            val yesterdayStr = dateFormat.format(currentDate.time)
            if (completedDates.contains(yesterdayStr)) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                return 0
            }
        }

        while (true) {
            val dateStr = dateFormat.format(currentDate.time)
            if (completedDates.contains(dateStr)) {
                streak++
                currentDate.add(Calendar.DAY_OF_YEAR, -1)
            } else {
                break
            }
        }

        return streak
    }

    private fun loadCompletedTaskDates() {
        viewModelScope.launch {
            taskRepository.tasks.collect { tasks ->
                val dates = tasks.filter { it.isCompleted && it.dateString.isNotBlank() }
                                 .map { it.dateString }
                                 .toSet()
                _completedTaskDates.value = dates
            }
        }
    }

    private fun loadBadges() {
        val badges = BadgeCatalogue.badges.mapIndexed { index, badge ->
            BadgeState(badge, isUnlocked = index < 5)
        }
        _uiState.value = _uiState.value.copy(unlockedBadges = badges)
    }

    private fun loadUserProfile() {
        val user = firebaseAuth.currentUser
        val email = user?.email ?: ""
        val emailPrefix = email.substringBefore("@")
        val fbName = user?.displayName?.takeIf { it.isNotBlank() } ?: emailPrefix.replaceFirstChar { it.uppercase() }

        viewModelScope.launch {
            combine(
                userPreferencesRepository.displayName,
                userPreferencesRepository.avatarId
            ) { namePref, avatarIdPref ->
                val finalName = namePref?.takeIf { it.isNotBlank() } ?: fbName
                val finalAvatarInitial = if (finalName.isNotBlank()) finalName.first().uppercase() else "U"
                val avatar = AvatarCatalogue.getAvatarById(avatarIdPref)
                
                _uiState.value = _uiState.value.copy(
                    avatarInitial = finalAvatarInitial,
                    avatar = avatar
                )
            }.collect { }
        }
    }

    fun fetchTasksForDate(dateString: String) {
        viewModelScope.launch {
            taskRepository.getTasksForDate(dateString).collect { tasks ->
                _selectedDateTasks.value = tasks.filter { it.isCompleted }
            }
        }
    }
}
