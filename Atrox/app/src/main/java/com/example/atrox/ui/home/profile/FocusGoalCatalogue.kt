package com.example.atrox.ui.home.profile

object FocusGoalCatalogue {
    val goals = listOf(
        FocusGoal("Deep Work", "🚀"),
        FocusGoal("Coding", "💻"),
        FocusGoal("Reading", "📚"),
        FocusGoal("Studying", "📖"),
        FocusGoal("Creative Work", "🎨"),
        FocusGoal("Writing", "✍️"),
        FocusGoal("Design", "✨"),
        FocusGoal("Meditation", "🧘"),
        FocusGoal("Exercise", "🏃‍♂️"),
        FocusGoal("Planning", "🗓️"),
        FocusGoal("Research", "🔍"),
        FocusGoal("Language Learning", "🌍"),
        FocusGoal("Music Practice", "🎸"),
        FocusGoal("Brainstorming", "💡"),
        FocusGoal("Administration", "📁")
    )

    fun getEmojiForGoal(label: String): String {
        return goals.find { it.label.equals(label, ignoreCase = true) }?.emoji ?: "🎯"
    }
}
