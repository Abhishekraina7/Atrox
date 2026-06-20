package com.example.atrox.ui.home.notes

import android.content.Context
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

/**
 * Snapshot of the note editor state used for undo/redo (Memento).
 */
data class NoteMemento(
    val title: String,
    val body: String,
    val attachedImages: List<String>
)

data class AddNoteUiState(
    val title: String = "",
    val body: String = "",
    val attachedImages: List<String> = emptyList(), // internal storage file paths
    val undoStack: List<NoteMemento> = emptyList(),
    val redoStack: List<NoteMemento> = emptyList(),
    val isSaved: Boolean = false
)

@HiltViewModel
class AddNotesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context
) : ViewModel() {

    private val _uiState = MutableStateFlow(AddNoteUiState())
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()

    // ── Text mutations ──────────────────────────────────────────────

    fun updateTitle(newTitle: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
            redoStack = emptyList(),
            title = newTitle
        )
    }

    fun updateBody(newBody: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
            redoStack = emptyList(),
            body = newBody
        )
    }

    // ── Image attachments ───────────────────────────────────────────

    /**
     * Copies the selected images from content URIs into the app's private
     * internal storage directory ("note_images/") and appends the resulting
     * file paths to [AddNoteUiState.attachedImages].
     */
    fun addImagesFromUris(uris: List<Uri>) {
        viewModelScope.launch(Dispatchers.IO) {
            val savedPaths = uris.mapNotNull { uri -> copyToInternalStorage(uri) }
            if (savedPaths.isNotEmpty()) {
                val current = _uiState.value
                _uiState.value = current.copy(
                    undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
                    redoStack = emptyList(),
                    attachedImages = current.attachedImages + savedPaths
                )
            }
        }
    }

    fun removeImage(path: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
            redoStack = emptyList(),
            attachedImages = current.attachedImages - path
        )
    }

    // ── Undo / Redo ─────────────────────────────────────────────────

    fun undo() {
        val current = _uiState.value
        if (current.undoStack.isNotEmpty()) {
            val previous = current.undoStack.last()
            _uiState.value = current.copy(
                title = previous.title,
                body = previous.body,
                attachedImages = previous.attachedImages,
                undoStack = current.undoStack.dropLast(1),
                redoStack = (current.redoStack + current.toMemento()).takeLast(MAX_STACK_SIZE)
            )
        }
    }

    fun redo() {
        val current = _uiState.value
        if (current.redoStack.isNotEmpty()) {
            val next = current.redoStack.last()
            _uiState.value = current.copy(
                title = next.title,
                body = next.body,
                attachedImages = next.attachedImages,
                undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
                redoStack = current.redoStack.dropLast(1)
            )
        }
    }

    // ── Helpers ──────────────────────────────────────────────────────

    /**
     * Copies a content:// URI to the app's private "note_images" directory.
     * Returns the absolute path of the saved file, or null on failure.
     */
    private fun copyToInternalStorage(uri: Uri): String? {
        return try {
            val imagesDir = File(appContext.filesDir, "note_images").apply { mkdirs() }
            val destFile = File(imagesDir, "${UUID.randomUUID()}.jpg")
            appContext.contentResolver.openInputStream(uri)?.use { input ->
                destFile.outputStream().use { output -> input.copyTo(output) }
            }
            destFile.absolutePath
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun AddNoteUiState.toMemento() = NoteMemento(
        title = title,
        body = body,
        attachedImages = attachedImages
    )

    companion object {
        private const val MAX_STACK_SIZE = 50
    }
}
