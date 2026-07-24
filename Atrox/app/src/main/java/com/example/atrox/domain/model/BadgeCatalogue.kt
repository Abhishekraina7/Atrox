package com.example.atrox.domain.model

import androidx.compose.ui.graphics.Color

object BadgeCatalogue {
    val badges = listOf(
        // Difficulty 1-5 (Beginner)
        AppBadge("b1", "First Step", "Complete your very first focus sprint", "🌱", Color(0xFF4CAF50), 1),
        AppBadge("b2", "Warming Up", "Accumulate 2 hours of total focus time", "🔥", Color(0xFFFF9800), 2),
        AppBadge("b3", "Consistency", "Maintain a 3-day focus streak", "📅", Color(0xFF42A5F5), 3),
        AppBadge("b4", "Early Bird", "Complete a sprint before 8 AM", "🌅", Color(0xFFFFEB3B), 4),
        AppBadge("b5", "Night Owl", "Complete a sprint after 10 PM", "🦉", Color(0xFF9C27B0), 5),

        // Difficulty 6-10 (Intermediate)
        AppBadge("b6", "Deep Diver", "Accumulate 10 hours of total focus time", "🌊", Color(0xFF03A9F4), 6),
        AppBadge("b7", "Unbreakable", "Maintain a 7-day focus streak", "🛡️", Color(0xFF795548), 7),
        AppBadge("b8", "Weekend Warrior", "Complete 5 sprints over a single weekend", "⚔️", Color(0xFFF44336), 8),
        AppBadge("b9", "Sprinter", "Complete 10 sprints in a single day", "⚡", Color(0xFFFFC107), 9),
        AppBadge("b10", "Century Club", "Complete 100 total sprints", "💯", Color(0xFFE91E63), 10),

        // Difficulty 11-15 (Advanced)
        AppBadge("b11", "Perfect Week", "Hit your daily sprint goal 7 days in a row", "⭐", Color(0xFFFFD700), 11),
        AppBadge("b12", "Workaholic", "Accumulate 50 hours of total focus time", "💼", Color(0xFF607D8B), 12),
        AppBadge("b13", "Half Marathon", "Maintain a 14-day focus streak", "🏃", Color(0xFF8BC34A), 13),
        AppBadge("b14", "Laser Focus", "Complete a 120-minute uninterrupted sprint", "🎯", Color(0xFFE53935), 14),
        AppBadge("b15", "Elite Focus", "Accumulate 100 hours of total focus time", "💎", Color(0xFF00BCD4), 15),

        // Difficulty 16-20 (Expert)
        AppBadge("b16", "Marathoner", "Maintain a 30-day focus streak", "🏅", Color(0xFFFF5722), 16),
        AppBadge("b17", "Grandmaster", "Complete 500 total sprints", "👑", Color(0xFF673AB7), 17),
        AppBadge("b18", "Titanium", "Maintain a 60-day focus streak", "🦾", Color(0xFF9E9E9E), 18),
        AppBadge("b19", "Relentless", "Hit your daily sprint goal 30 days in a row", "🌪️", Color(0xFF3F51B5), 19),
        AppBadge("b20", "Time Lord", "Accumulate 500 hours of total focus time", "⏳", Color(0xFF009688), 20),

        // Difficulty 21-25 (Master)
        AppBadge("b21", "Mythic", "Maintain a 100-day focus streak", "🐉", Color(0xFFD32F2F), 21),
        AppBadge("b22", "The 1%", "Complete 1,000 total sprints", "📈", Color(0xFF8E24AA), 22),
        AppBadge("b23", "Untouchable", "Hit your daily sprint goal 100 days in a row", "✨", Color(0xFF00E676), 23),
        AppBadge("b24", "Ascended", "Accumulate 1,000 hours of total focus time", "🌌", Color(0xFF1A237E), 24),
        AppBadge("b25", "Atrox Prime", "Maintain a 365-day focus streak (1 Year)", "🏆", Color(0xFFFFD700), 25)
    )
}
