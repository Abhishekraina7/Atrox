package com.example.atrox.ui.home.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.preferences.UserPreferencesRepository
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch


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
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        preferencesRepository.sprintDuration.onEach { duration ->
            _uiState.value = _uiState.value.copy(sprintDuration = duration)
        }.launchIn(viewModelScope)

        preferencesRepository.breakDuration.onEach { duration ->
            _uiState.value = _uiState.value.copy(breakDuration = duration)
        }.launchIn(viewModelScope)

        preferencesRepository.dailySprints.onEach { goal ->
            _uiState.value = _uiState.value.copy(dailySprintGoal = goal)
        }.launchIn(viewModelScope)

        preferencesRepository.autoStartNextSprint.onEach { autoStart ->
            _uiState.value = _uiState.value.copy(autoStartNextSprint = autoStart)
        }.launchIn(viewModelScope)

        preferencesRepository.blockNotifications.onEach { block ->
            _uiState.value = _uiState.value.copy(blockNotifications = block)
        }.launchIn(viewModelScope)

        preferencesRepository.approvalForEarlyExit.onEach { approval ->
            _uiState.value = _uiState.value.copy(approvalForEarlyExit = approval)
        }.launchIn(viewModelScope)

        preferencesRepository.sprintReminders.onEach { reminders ->
            _uiState.value = _uiState.value.copy(sprintReminders = reminders)
        }.launchIn(viewModelScope)

        preferencesRepository.dailyGoalNudge.onEach { nudge ->
            _uiState.value = _uiState.value.copy(dailyGoalNudge = nudge)
        }.launchIn(viewModelScope)
    }

    fun updateSprintDuration(duration: Int) {
        viewModelScope.launch {
            preferencesRepository.setSprintDuration(duration)
        }
    }

    fun updateBreakDuration(duration: Int) {
        viewModelScope.launch {
            preferencesRepository.setBreakDuration(duration)
        }
    }

    fun updateDailySprintGoal(goal: Int) {
        viewModelScope.launch {
            preferencesRepository.setSprintGoal(goal)
        }
    }

    fun toggleAutoStartNextSprint() {
        viewModelScope.launch {
            preferencesRepository.setAutoStartNextSprint(!_uiState.value.autoStartNextSprint)
        }
    }

    fun toggleBlockNotifications(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setBlockNotifications(enabled)
        }
    }

    fun toggleBlockSocialApps() {
        _uiState.value = _uiState.value.copy(blockSocialApps = !_uiState.value.blockSocialApps)
    }

    fun toggleAllowEmergencyCalls() {
        _uiState.value = _uiState.value.copy(allowEmergencyCalls = !_uiState.value.allowEmergencyCalls)
    }

    fun toggleApprovalForEarlyExit() {
        viewModelScope.launch {
            preferencesRepository.setApprovalForEarlyExit(!_uiState.value.approvalForEarlyExit)
        }
    }

    fun toggleSprintReminders() {
        viewModelScope.launch {
            preferencesRepository.setSprintReminders(!_uiState.value.sprintReminders)
        }
    }

    fun toggleDailyGoalNudge() {
        viewModelScope.launch {
            preferencesRepository.setDailyGoalNudge(!_uiState.value.dailyGoalNudge)
        }
    }

    fun toggleHapticFeedback() {
        _uiState.value = _uiState.value.copy(hapticFeedback = !_uiState.value.hapticFeedback)
    }
}
