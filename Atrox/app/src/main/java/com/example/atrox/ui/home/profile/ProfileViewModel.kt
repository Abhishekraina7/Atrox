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
import com.example.atrox.data.preferences.AppBadge
import com.example.atrox.data.preferences.FocusGoalCatalogue
import com.example.atrox.data.preferences.UserPreferencesRepository
import com.example.atrox.data.preferences.Avatar
import com.example.atrox.data.preferences.AvatarCatalogue
import com.example.atrox.data.preferences.BadgeCatalogue
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class Badge(
    val title: String,
    val emoji: String,
    val color: Color,
    val timeAgo: String
)

data class BadgeState(
    val badge: AppBadge,
    val isUnlocked: Boolean
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
    val avatar: Avatar? = null,
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
    val allBadges: List<BadgeState> = emptyList(),
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
        updateBadges()
    }

    private fun updateBadges() {
        val state = _uiState.value
        val badges = BadgeCatalogue.badges.map { badge ->
            val unlocked = when (badge.id) {
                "b1" -> state.sprints >= 1
                "b2" -> state.focusHours >= 2.0
                "b3" -> state.streakDays >= 3
                "b4" -> true
                "b5" -> true
                "b6" -> state.focusHours >= 10.0
                "b7" -> state.streakDays >= 7
                "b8" -> true
                "b9" -> true
                "b10" -> state.sprints >= 100
                else -> false
            }
            BadgeState(badge, unlocked)
        }
        _uiState.value = state.copy(allBadges = badges)
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

    fun updateProfile(name: String, avatarId: String?) {
        viewModelScope.launch {
            userPreferencesRepository.setDisplayName(name)
            if (avatarId != null) {
                userPreferencesRepository.setAvatarId(avatarId)
            }
        }
    }

    private fun loadUserProfile() {
        val user = firebaseAuth.currentUser
        val email = user?.email ?: ""
        val emailPrefix = email.substringBefore("@")
        val fbName = user?.displayName?.takeIf { it.isNotBlank() } ?: emailPrefix.replaceFirstChar { it.uppercase() }
        val handle = if (emailPrefix.isNotBlank()) "@$emailPrefix" else ""

        viewModelScope.launch {
            combine(
                userPreferencesRepository.displayName,
                userPreferencesRepository.avatarId
            ) { namePref, avatarIdPref ->
                val finalName = namePref?.takeIf { it.isNotBlank() } ?: fbName
                val finalAvatarInitial = if (finalName.isNotBlank()) finalName.first().uppercase() else "U"
                val avatar = AvatarCatalogue.getAvatarById(avatarIdPref)
                
                _uiState.value = _uiState.value.copy(
                    name = finalName.ifBlank { "User" },
                    handle = handle,
                    avatarInitial = finalAvatarInitial,
                    avatar = avatar
                )
            }.collect { }
        }
    }
}
