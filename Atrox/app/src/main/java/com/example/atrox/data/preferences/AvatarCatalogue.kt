package com.example.atrox.data.preferences

import androidx.compose.ui.graphics.Color

data class Avatar(
    val id: String,
    val emoji: String,
    val gradientColors: List<Color>
)

object AvatarCatalogue {
    val avatars = listOf(
        Avatar("a1", "🦊", listOf(Color(0xFFFF9800), Color(0xFFFF5722))),
        Avatar("a2", "🐼", listOf(Color(0xFF607D8B), Color(0xFF263238))),
        Avatar("a3", "🐯", listOf(Color(0xFFFFC107), Color(0xFFFF9800))),
        Avatar("a4", "🦁", listOf(Color(0xFFFF5722), Color(0xFFE64A19))),
        Avatar("a5", "🐸", listOf(Color(0xFF8BC34A), Color(0xFF388E3C))),
        Avatar("a6", "🦄", listOf(Color(0xFFE040FB), Color(0xFF7C4DFF))),
        Avatar("a7", "🦉", listOf(Color(0xFF795548), Color(0xFF4E342E))),
        Avatar("a8", "🐙", listOf(Color(0xFFE91E63), Color(0xFF880E4F))),
        Avatar("a9", "🐝", listOf(Color(0xFFFFEB3B), Color(0xFFF57F17))),
        Avatar("a10", "🦋", listOf(Color(0xFF03A9F4), Color(0xFF0277BD))),
        Avatar("a11", "🐢", listOf(Color(0xFF4CAF50), Color(0xFF1B5E20))),
        Avatar("a12", "🐺", listOf(Color(0xFF9E9E9E), Color(0xFF424242)))
    )

    fun getAvatarById(id: String?): Avatar? {
        return avatars.find { it.id == id }
    }
}
