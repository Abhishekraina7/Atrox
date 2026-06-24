package com.example.atrox.ui.home.profile

import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import com.google.firebase.auth.FirebaseAuth
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.preferences.FocusGoalCatalogue
import com.example.atrox.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

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
class ProfileViewModel @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val userPreferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        loadUserProfile()
        loadPreferences()
    }

    private fun loadPreferences() {
        viewModelScope.launch {
            combine(
                userPreferencesRepository.focusGoals,
                userPreferencesRepository.primaryGoal
            ) { goalsSet, primary ->
                if (goalsSet.isNotEmpty()) {
                    goalsSet.map { goalLabel ->
                        FocusGoal(goalLabel, FocusGoalCatalogue.getEmojiForGoal(goalLabel))
                    }
                } else {
                    listOf(FocusGoal(primary, FocusGoalCatalogue.getEmojiForGoal(primary)))
                }
            }.collect { focusGoalsList ->
                _uiState.value = _uiState.value.copy(focusGoals = focusGoalsList)
            }
        }
    }

    fun updateFocusGoals(newGoals: Set<String>) {
        viewModelScope.launch {
            userPreferencesRepository.setFocusGoals(newGoals)
        }
    }

    private fun loadUserProfile() {
        val user = firebaseAuth.currentUser
        if (user != null) {
            val email = user.email ?: ""
            val emailPrefix = email.substringBefore("@")
            val name = user.displayName?.takeIf { it.isNotBlank() } ?: emailPrefix.replaceFirstChar { it.uppercase() }
            val handle = if (emailPrefix.isNotBlank()) "@$emailPrefix" else ""
            val avatarInitial = if (name.isNotBlank()) name.first().uppercase() else "U"

            _uiState.value = _uiState.value.copy(
                name = name.ifBlank { "User" },
                handle = handle,
                avatarInitial = avatarInitial
            )
        }
    }
}
