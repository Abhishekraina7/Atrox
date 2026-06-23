package com.example.atrox.ui.home.notes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.notes.NoteRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NoteCategory.ALL)
    val selectedCategory: StateFlow<NoteCategory> = _selectedCategory.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())

    val notes: StateFlow<List<NoteItem>> = noteRepository.getAllNotes()
        .map { entities ->
            entities.map { entity ->
                NoteItem(
                    id = entity.id,
                    title = entity.title,
                    content = entity.content,
                    timestamp = dateFormat.format(Date(entity.timestamp)).uppercase(),
                    hasAudio = entity.hasAudio,
                    isSpanning = entity.isSpanning,
                    category = entity.category
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun selectCategory(category: NoteCategory) {
        _selectedCategory.value = category
    }
}
