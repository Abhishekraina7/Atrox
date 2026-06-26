package com.example.atrox.ui.home.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class DayStatus {
    ACTIVE,
    TODAY,
    INACTIVE,
    EMPTY // Empty space for days of the week before the 1st of the month
}

data class DayHistory(
    val dayNumber: Int,
    val status: DayStatus
)

data class MonthHistory(
    val monthName: String,
    val activeCountText: String,
    val startingDayOfWeek: Int, // 1 = Sunday, 2 = Monday, ..., 7 = Saturday
    val days: List<DayHistory>
)

data class StreakHistoryUiState(
    val currentStreak: Int = 14,
    val consistency: Int = 88,
    val calendarHistory: List<MonthHistory> = emptyList()
)

@HiltViewModel
class StreakHistoryViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(StreakHistoryUiState())
    val uiState: StateFlow<StreakHistoryUiState> = _uiState.asStateFlow()

    init {
        generateHistoryData()
    }

    private fun generateHistoryData() {
        // March 2024
        // Starting day of March 2024 is Friday (6)
        // Active days: 1, 2, 3, 4, 5, 8, 9, 10 (ACTIVE), 11 (TODAY)
        val marchDays = mutableListOf<DayHistory>()
        for (day in 1..31) {
            val status = when (day) {
                1, 2, 3, 4, 5, 8, 9, 10 -> DayStatus.ACTIVE
                11 -> DayStatus.TODAY
                else -> DayStatus.INACTIVE
            }
            marchDays.add(DayHistory(day, status))
        }

        // February 2024 (Leap year)
        // Starting day of Feb 2024 is Thursday (5)
        // Active days: 1 to 29 (all active)
        val febDays = mutableListOf<DayHistory>()
        for (day in 1..29) {
            febDays.add(DayHistory(day, DayStatus.ACTIVE))
        }

        // January 2024
        // Starting day of Jan 2024 is Monday (2)
        // Active days: 4 to 15, 19 to 21
        val janDays = mutableListOf<DayHistory>()
        for (day in 1..31) {
            val status = when (day) {
                in 4..15 -> DayStatus.ACTIVE
                in 19..21 -> DayStatus.ACTIVE
                else -> DayStatus.INACTIVE
            }
            janDays.add(DayHistory(day, status))
        }

        val history = listOf(
            MonthHistory(
                monthName = "March 2024",
                activeCountText = "21 Active",
                startingDayOfWeek = 6, // Friday
                days = marchDays
            ),
            MonthHistory(
                monthName = "February 2024",
                activeCountText = "29 Active",
                startingDayOfWeek = 5, // Thursday
                days = febDays
            ),
            MonthHistory(
                monthName = "January 2024",
                activeCountText = "15 Active",
                startingDayOfWeek = 2, // Monday
                days = janDays
            )
        )

        _uiState.value = StreakHistoryUiState(
            currentStreak = 14,
            consistency = 88,
            calendarHistory = history
        )
    }
}
