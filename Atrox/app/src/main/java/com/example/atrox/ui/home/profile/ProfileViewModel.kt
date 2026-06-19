package com.example.atrox.ui.home.profile

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class Badge(
    val title: String,
    val emoji: String,
    val color: Color,
    val timeAgo: String
)

data class SettingsItem(
    val title: String,
    val subtitle: String,
    val iconEmoji: String,
    val iconColor: Color
)

data class FocusGoal(
    val label: String,
    val emoji: String
)

data class ProfileUiState(
    val name: String = "Alex Rivers",
    val handle: String = "@arivers_focus",
    val memberSince: String = "Member since October 2023",
    val avatarInitial: String = "A",
    val sprints: Int = 128,
    val focusHours: Double = 452.5,
    val streakDays: Int = 14,
    val focusGoals: List<FocusGoal> = listOf(
        FocusGoal("Deep Work", "💻"),
        FocusGoal("Coding", "⚙️"),
        FocusGoal("Reading", "📖"),
        FocusGoal("Design", "🎨")
    ),
    val badges: List<Badge> = listOf(
        Badge("Early Riser", "🏆", Color(0xFFFF9800), "3d ago"),
        Badge("Century Club", "⚡", Color(0xFF6C63FF), "1w ago"),
        Badge("Deep Diver", "✨", Color(0xFF42A5F5), "2w ago")
    ),
    val settingsItems: List<SettingsItem> = listOf(
        SettingsItem("My Regulator", "Manage productivity thresholds", "🛡️", Color(0xFF42A5F5)),
        SettingsItem("Streak History", "View performance timeline", "🔥", Color(0xFFFF9800)),
        SettingsItem("Export Data", "Download focus logs (CSV/JSON)", "📦", Color(0xFF9C27B0))
    )
)

@HiltViewModel
class ProfileViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()
}
