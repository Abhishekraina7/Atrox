package com.example.atrox.ui.home.focus

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.atrox.ui.home.profile.BadgeState
import com.example.atrox.data.preferences.BadgeCatalogue
import com.example.atrox.data.preferences.Avatar
import com.example.atrox.data.preferences.AvatarCatalogue
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.data.tasks.TaskRepository
import com.example.atrox.data.tasks.TaskItem
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.combine
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

data class FocusDashboardUiState(
    val avatar: Avatar? = null,
    val avatarInitial: String = "U",
    val weeklyTotalHours: Double = 42.5,
    val weeklyAverageHours: Double = 6.1,
    val weeklyBestHours: Double = 8.4,
    val currentStreak: Int = 14,
    val longestStreak: Int = 28,
    val consistencyScore: Int = 94,
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

    init {
        loadBadges()
        loadUserProfile()
        loadCompletedTaskDates()
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
