package com.example.atrox.service.regulator

import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RegulatorManager @Inject constructor() {
    
    private val _approvalEvents = MutableSharedFlow<Unit>(extraBufferCapacity = 1)
    val approvalEvents = _approvalEvents.asSharedFlow()

    fun triggerApproval() {
        _approvalEvents.tryEmit(Unit)
    }
}
