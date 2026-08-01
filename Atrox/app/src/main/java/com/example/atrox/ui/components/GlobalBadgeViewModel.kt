package com.example.atrox.ui.components

import androidx.lifecycle.ViewModel
import com.example.atrox.domain.engine.BadgeEngine
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class GlobalBadgeViewModel @Inject constructor(
    badgeEngine: BadgeEngine
) : ViewModel() {
    val newlyUnlockedBadgeEvent = badgeEngine.newlyUnlockedBadgeEvent
}
