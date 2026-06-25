package com.example.atrox.ui.home.focus

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.example.atrox.ui.home.profile.BadgeState
import com.example.atrox.data.preferences.BadgeCatalogue

data class FocusDashboardUiState(
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
class FocusViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(FocusDashboardUiState())
    val uiState = _uiState.asStateFlow()

    init {
        loadBadges()
    }

    private fun loadBadges() {
        val badges = BadgeCatalogue.badges.mapIndexed { index, badge ->
            BadgeState(badge, isUnlocked = index < 5)
        }
        _uiState.value = _uiState.value.copy(unlockedBadges = badges)
    }
}
