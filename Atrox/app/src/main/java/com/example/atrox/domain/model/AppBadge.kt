package com.example.atrox.domain.model

import androidx.compose.ui.graphics.Color

data class AppBadge(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val color: Color,
    val difficultyLevel: Int // 1 to 25
)
