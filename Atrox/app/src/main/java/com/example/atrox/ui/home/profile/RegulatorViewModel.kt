package com.example.atrox.ui.home.profile

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class RegulatorUiState(
    val hasRegulator: Boolean = true,
    val name: String = "Sarah Jenkins",
    val handle: String = "@sarah_j",
    val status: String = "Monitoring",
    val connectedSince: String = "OCT 2023"
)

@HiltViewModel
class RegulatorViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(RegulatorUiState())
    val uiState: StateFlow<RegulatorUiState> = _uiState.asStateFlow()

    fun addRegulator(name: String, handle: String) {
        _uiState.value = RegulatorUiState(
            hasRegulator = true,
            name = name,
            handle = handle,
            status = "Monitoring",
            connectedSince = "OCT 2023"
        )
    }

    fun removeRegulator() {
        _uiState.value = _uiState.value.copy(hasRegulator = false)
    }
}
