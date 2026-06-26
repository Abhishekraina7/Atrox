package com.example.atrox.ui.home.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class SettingsUiState(
    val sprintDuration: Int = 25,
    val breakDuration: Int = 5,
    val dailySprintGoal: Int = 8,
    val autoStartNextSprint: Boolean = true,
    
    val blockNotifications: Boolean = true,
    val blockSocialApps: Boolean = false,
    val allowEmergencyCalls: Boolean = true,
    
    val approvalForEarlyExit: Boolean = false,
    
    val sprintReminders: Boolean = true,
    val dailyGoalNudge: Boolean = true,
    
    val hapticFeedback: Boolean = true,
    val theme: String = "MIDNIGHT (DARK)",
    val appVersion: String = "v2.4.12-pro"
)

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun toggleAutoStartNextSprint() {
        _uiState.value = _uiState.value.copy(autoStartNextSprint = !_uiState.value.autoStartNextSprint)
    }

    fun toggleBlockNotifications() {
        _uiState.value = _uiState.value.copy(blockNotifications = !_uiState.value.blockNotifications)
    }

    fun toggleBlockSocialApps() {
        _uiState.value = _uiState.value.copy(blockSocialApps = !_uiState.value.blockSocialApps)
    }

    fun toggleAllowEmergencyCalls() {
        _uiState.value = _uiState.value.copy(allowEmergencyCalls = !_uiState.value.allowEmergencyCalls)
    }

    fun toggleApprovalForEarlyExit() {
        _uiState.value = _uiState.value.copy(approvalForEarlyExit = !_uiState.value.approvalForEarlyExit)
    }

    fun toggleSprintReminders() {
        _uiState.value = _uiState.value.copy(sprintReminders = !_uiState.value.sprintReminders)
    }

    fun toggleDailyGoalNudge() {
        _uiState.value = _uiState.value.copy(dailyGoalNudge = !_uiState.value.dailyGoalNudge)
    }

    fun toggleHapticFeedback() {
        _uiState.value = _uiState.value.copy(hapticFeedback = !_uiState.value.hapticFeedback)
    }
}
