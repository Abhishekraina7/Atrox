package com.example.atrox.ui.home.notes

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

data class AddNoteUiState(
    val title: String = "",
    val body: String = "",
    val undoStack: List<Pair<String, String>> = emptyList(), // (title, body) snapshots
    val redoStack: List<Pair<String, String>> = emptyList(),
    val isSaved: Boolean = false
)

@HiltViewModel
class AddNotesViewModel @Inject constructor() : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    fun updateTitle(newTitle: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            undoStack = current.undoStack + (current.title to current.body),
            redoStack = emptyList(),
            title = newTitle
        )
    }

    fun updateBody(newBody: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            undoStack = current.undoStack + (current.title to current.body),
            redoStack = emptyList(),
            body = newBody
        )
    }

    fun undo() {
        val current = _uiState.value
        if (current.undoStack.isNotEmpty()) {
            val previous = current.undoStack.last()
            _uiState.value = current.copy(
                title = previous.first,
                body = previous.second,
                undoStack = current.undoStack.dropLast(1),
                redoStack = current.redoStack + (current.title to current.body)
            )
        }
    }

    fun redo() {
        val current = _uiState.value
        if (current.redoStack.isNotEmpty()) {
            val next = current.redoStack.last()
            _uiState.value = current.copy(
                title = next.first,
                body = next.second,
                undoStack = current.undoStack + (current.title to current.body),
                redoStack = current.redoStack.dropLast(1)
            )
        }
    }

    val canUndo: Boolean get() = _uiState.value.undoStack.isNotEmpty()
    val canRedo: Boolean get() = _uiState.value.redoStack.isNotEmpty()
}
