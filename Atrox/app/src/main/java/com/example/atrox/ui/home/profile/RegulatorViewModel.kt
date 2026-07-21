package com.example.atrox.ui.home.profile

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.repository.RegulatorRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import javax.inject.Inject

data class RegulatorUiState(
    val hasRegulator: Boolean = false,
    val name: String = "",
    val phone: String = "",
    val status: String = "Monitoring",
    val connectedSince: String = "OCT 2023"
)

@HiltViewModel
class RegulatorViewModel @Inject constructor(
    private val regulatorRepository: RegulatorRepository
) : ViewModel() {
    private val _uiState = MutableStateFlow(RegulatorUiState())
    val uiState: StateFlow<RegulatorUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                regulatorRepository.guardianName,
                regulatorRepository.guardianPhone
            ) { name, phone ->
                Pair(name, phone)
            }.collect { (name, phone) ->
                val isValidRegulator = !name.isNullOrBlank() || !phone.isNullOrBlank()
                _uiState.value = RegulatorUiState(
                    hasRegulator = isValidRegulator,
                    name = name ?: "Unknown",
                    phone = phone ?: "Unknown",
                    status = if (isValidRegulator) "Monitoring" else "Inactive",
                    connectedSince = "OCT 2023"
                )
            }
        }
    }

    fun addRegulator(name: String, phone: String) {
        viewModelScope.launch {
            regulatorRepository.saveGuardianName(name)
            regulatorRepository.saveGuardianPhone(phone)
        }
    }

    fun removeRegulator() {
        viewModelScope.launch {
            regulatorRepository.clearGuardian()
        }
    }
}
