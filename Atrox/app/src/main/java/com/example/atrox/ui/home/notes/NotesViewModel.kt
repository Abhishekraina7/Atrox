package com.example.atrox.ui.home.notes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

enum class NoteCategory {
    ALL, PERSONAL, JOURNAL, WORK
}

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val hasAudio: Boolean = false,
    val isSpanning: Boolean = false, // for the full-width card with image
    val category: NoteCategory = NoteCategory.PERSONAL
)

@HiltViewModel
class NotesViewModel @Inject constructor() : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NoteCategory.ALL)
    val selectedCategory: StateFlow<NoteCategory> = _selectedCategory.asStateFlow()

    private val _notes = MutableStateFlow(
        listOf(
            NoteItem(
                id = "1",
                title = "Product...",
                content = "Focus on user-centric design...",
                timestamp = "12 OCT • 14:30",
                hasAudio = true,
                category = NoteCategory.WORK
            ),
            NoteItem(
                id = "2",
                title = "Morning...",
                content = "Woke up feeling energized. The...",
                timestamp = "12 OCT • 08:15",
                category = NoteCategory.JOURNAL
            ),
            NoteItem(
                id = "3",
                title = "Project Alpha...",
                content = "Finalize the quarterly projections and se...",
                timestamp = "11 OCT • 16:45",
                category = NoteCategory.WORK
            ),
            NoteItem(
                id = "4",
                title = "Grocery List",
                content = "Almond milk, kale, salmon, blueberrie...",
                timestamp = "11 OCT • 10:20",
                hasAudio = true,
                category = NoteCategory.PERSONAL
            ),
            NoteItem(
                id = "5",
                title = "Inspiration Board",
                content = "Moodboard for the new mobile interface project.",
                timestamp = "10 OCT • 18:00",
                isSpanning = true,
                category = NoteCategory.WORK
            )
        )
    )
    val notes: StateFlow<List<NoteItem>> = _notes.asStateFlow()

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: NoteCategory) {
        _selectedCategory.value = category
    }
}
