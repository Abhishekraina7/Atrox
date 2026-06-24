package com.example.atrox.ui.home.notes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.ExperimentalCoroutinesApi
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.notes.NoteRepository
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

enum class NoteCategory {
    ALL, PERSONAL, JOURNAL, WORK
}

enum class SortOption {
    TIME_CREATED_DESC,
    TIME_CREATED_ASC
}

data class NoteItem(
    val id: String,
    val title: String,
    val content: String,
    val timestamp: String,
    val rawTimestamp: Long,
    val hasAudio: Boolean = false,
    val isSpanning: Boolean = false, // for the full-width card with image
    val category: NoteCategory = NoteCategory.PERSONAL,
    val isPinned: Boolean = false
)

@HiltViewModel
class NotesViewModel @Inject constructor(
    private val noteRepository: NoteRepository
) : ViewModel() {
    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _submittedQuery = MutableStateFlow("")
    val submittedQuery: StateFlow<String> = _submittedQuery.asStateFlow()

    private val _selectedCategory = MutableStateFlow(NoteCategory.ALL)
    val selectedCategory: StateFlow<NoteCategory> = _selectedCategory.asStateFlow()

    private val _sortOption = MutableStateFlow(SortOption.TIME_CREATED_DESC)
    val sortOption: StateFlow<SortOption> = _sortOption.asStateFlow()

    private val dateFormat = SimpleDateFormat("dd MMM • HH:mm", Locale.getDefault())

    @OptIn(ExperimentalCoroutinesApi::class)
    val notes: StateFlow<List<NoteItem>> = _submittedQuery
        .flatMapLatest { query ->
            if (query.isBlank()) {
                noteRepository.getAllNotes()
            } else {
                noteRepository.searchNotesByTitle(query)
            }
        }
        .map { entities ->
            entities.map { entity ->
                NoteItem(
                    id = entity.id,
                    title = entity.title,
                    content = entity.content,
                    timestamp = dateFormat.format(Date(entity.timestamp)).uppercase(),
                    rawTimestamp = entity.timestamp,
                    hasAudio = entity.hasAudio,
                    isSpanning = entity.isSpanning,
                    category = entity.category,
                    isPinned = entity.isPinned
                )
            }
        }.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
        if (query.isBlank()) {
            submitSearchQuery("")
        }
    }

    fun submitSearchQuery(query: String) {
        _submittedQuery.value = query
    }

    fun resetSearch() {
        _searchQuery.value = ""
        _submittedQuery.value = ""
    }

    fun selectCategory(category: NoteCategory) {
        _selectedCategory.value = category
    }

    fun updateSortOption(option: SortOption) {
        _sortOption.value = option
    }
}
