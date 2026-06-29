package com.example.atrox.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

import com.example.atrox.service.regulator.RegulatorRepository

@HiltViewModel
class Onboarding4ViewModel @Inject constructor(
    private val regulatorRepository: RegulatorRepository
) : ViewModel() {

    private val _events = MutableSharedFlow<Onboarding4Event>()
    val events: SharedFlow<Onboarding4Event> = _events.asSharedFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _countryCode = MutableStateFlow("+91")
    val countryCode: StateFlow<String> = _countryCode.asStateFlow()

    private val _regulatorName = MutableStateFlow("")
    val regulatorName: StateFlow<String> = _regulatorName.asStateFlow()

    fun onSearchQueryChanged(query: String) {
        val digitsOnly = query.filter { it.isDigit() }
        _searchQuery.value = digitsOnly.take(10)
    }

    fun onCountryCodeChanged(code: String) {
        _countryCode.value = code
    }

    fun onRegulatorNameChanged(name: String) {
        _regulatorName.value = name
    }

    fun onBackClicked() {
        emitEvent(Onboarding4Event.NavigateBack)
    }

    fun onSkipClicked() {
        emitEvent(Onboarding4Event.NavigateToSkip)
    }

    fun onContinueClicked() {
        viewModelScope.launch {
            val phone = _searchQuery.value.trim()
            val code = _countryCode.value.trim()
            val name = _regulatorName.value.trim()
            if (phone.isNotBlank()) {
                val fullPhone = "$code $phone"
                regulatorRepository.saveGuardianPhone(fullPhone)
            }
            if (name.isNotBlank()) {
                regulatorRepository.saveGuardianName(name)
            }
            _events.emit(Onboarding4Event.NavigateToDashboard)
        }
    }

    private fun emitEvent(event: Onboarding4Event) {
        viewModelScope.launch {
            _events.emit(event)
        }
    }
}
