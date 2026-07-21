package com.example.atrox.domain.model

import androidx.compose.ui.graphics.Color

data class Avatar(
    val id: String,
    val emoji: String,
    val gradientColors: List<Color>
)
