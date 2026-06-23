package com.example.atrox.ui.home.notes

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.example.atrox.data.notes.NoteEntity
import com.example.atrox.data.notes.NoteRepository
import com.example.atrox.utils.SpeechRecognitionManager
import com.example.atrox.utils.SpeechState
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.firstOrNull
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
    val noteId: String? = null,
    val title: String = "",
    val body: String = "",
    val attachedImages: List<String> = emptyList(), // internal storage file paths
    val undoStack: List<NoteMemento> = emptyList(),
    val redoStack: List<NoteMemento> = emptyList(),
    val isSaved: Boolean = false,
    // ── Speech recognition state ──
    val speechState: SpeechState = SpeechState.Idle,
    /** Live preview text shown while the recognizer is still processing. */
    val partialSpeechText: String = ""
)

@HiltViewModel
class AddNotesViewModel @Inject constructor(
    @ApplicationContext private val appContext: Context,
    private val noteRepository: NoteRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val initialNoteId: String? = savedStateHandle["noteId"]

    private val _uiState = MutableStateFlow(AddNoteUiState(noteId = initialNoteId))
    val uiState: StateFlow<AddNoteUiState> = _uiState.asStateFlow()
    // To optimize for memory we limit the redo/undo feature to a upper limit of 50
    companion object {
        private const val MAX_STACK_SIZE = 50
    }

    // ── Speech Recognition ──────────────────────────────────────────

    private val speechManager = SpeechRecognitionManager(appContext)

    init {
        // Observe the speech manager's state and react accordingly
        viewModelScope.launch {
            speechManager.state.collect { speechState ->
                val partial = when (speechState) {
                    is SpeechState.Listening -> speechState.partialText
                    is SpeechState.Processing -> speechState.partialText
                    is SpeechState.Result -> speechState.text
                    else -> _uiState.value.partialSpeechText
                }

                _uiState.value = _uiState.value.copy(
                    speechState = speechState,
                    partialSpeechText = partial
                )
            }
        }

        // Load note if editing
        initialNoteId?.let { id ->
            viewModelScope.launch {
                val note = noteRepository.getNoteById(id).firstOrNull()
                if (note != null) {
                    _uiState.value = _uiState.value.copy(
                        title = note.title,
                        body = note.content,
                        attachedImages = if (note.attachedImages.isNotBlank()) note.attachedImages.split(",") else emptyList(),
                        isSaved = true
                    )
                }
            }
        }
    }

    /** Whether the device supports speech recognition at all. */
    fun isSpeechAvailable(): Boolean = speechManager.isAvailable()

    /** Check if device has an active internet connection */
    fun isNetworkAvailable(): Boolean {
        val connectivityManager = appContext.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false
        return when {
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true
            activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET) -> true
            else -> false
        }
    }

    /**
     * Start speech recognition. Must be called from the Main thread
     * (the Composable layer ensures this via the permission callback).
     */
    fun startSpeechRecognition() {
        // Clear any stale preview text
        _uiState.value = _uiState.value.copy(partialSpeechText = "")
        speechManager.startListening()
    }

    /** Stop speech recognition and discard any partial results. */
    fun stopSpeechRecognition() {
        speechManager.cancelListening()
        _uiState.value = _uiState.value.copy(
            speechState = SpeechState.Idle,
            partialSpeechText = ""
        )
    }

    /** Manually stop listening and process current audio. */
    fun finishSpeechRecognition() {
        speechManager.finishListening()
    }

    /**
     * Called by the UI when the user confirms the transcribed text.
     * Appends the recognized text to the note body (with a space separator).
     */
    fun acceptSpeechResult() {
        speechManager.cancelListening()
        val text = _uiState.value.partialSpeechText.trim()
        if (text.isNotEmpty()) {
            val current = _uiState.value
            val separator = if (current.body.isNotEmpty() && !current.body.endsWith(" ")) " " else ""
            val newBody = current.body + separator + text
            _uiState.value = current.copy(
                undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
                redoStack = emptyList(),
                body = newBody,
                speechState = SpeechState.Idle,
                partialSpeechText = ""
            )
        } else {
            _uiState.value = _uiState.value.copy(
                speechState = SpeechState.Idle,
                partialSpeechText = ""
            )
        }
    }

    /** Dismiss the speech UI without appending any text. */
    fun dismissSpeech() {
        speechManager.cancelListening()
        _uiState.value = _uiState.value.copy(
            speechState = SpeechState.Idle,
            partialSpeechText = ""
        )
    }

    override fun onCleared() {
        super.onCleared()
        speechManager.destroy()
    }

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

    /**
     * Adds an image that already exists at [path] in internal storage
     * (used by the camera flow where we pre-create the file).
     */
    fun addImageByPath(path: String) {
        val current = _uiState.value
        _uiState.value = current.copy(
            undoStack = (current.undoStack + current.toMemento()).takeLast(MAX_STACK_SIZE),
            redoStack = emptyList(),
            attachedImages = current.attachedImages + path
        )
    }

    /**
     * Creates a new empty file in "note_images/" and returns a Pair of
     * (absolutePath, contentUri) so the TakePicture contract can write to it.
     */
    fun createCameraImageFile(): Pair<String, android.net.Uri> {
        val imagesDir = File(appContext.filesDir, "note_images").apply { mkdirs() }
        val file = File(imagesDir, "${UUID.randomUUID()}.jpg")
        val uri = androidx.core.content.FileProvider.getUriForFile(
            appContext,
            "${appContext.packageName}.fileprovider",
            file
        )
        return file.absolutePath to uri
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

    fun saveNote() {
        val current = _uiState.value
        if (current.title.isNotBlank() || current.body.isNotBlank()) {
            _uiState.value = current.copy(isSaved = true)
            
            viewModelScope.launch {
                val idToSave = current.noteId ?: UUID.randomUUID().toString()
                _uiState.value = _uiState.value.copy(noteId = idToSave)
                
                val entity = NoteEntity(
                    id = idToSave,
                    title = current.title.ifBlank { "Untitled Note" },
                    content = current.body,
                    timestamp = System.currentTimeMillis(),
                    hasAudio = current.speechState !is SpeechState.Idle,
                    isSpanning = current.attachedImages.isNotEmpty(),
                    category = com.example.atrox.ui.home.notes.NoteCategory.PERSONAL,
                    attachedImages = current.attachedImages.joinToString(",")
                )
                noteRepository.insertNote(entity)
            }
        }
    }

    fun deleteNote() {
        val idToDelete = _uiState.value.noteId
        if (idToDelete != null) {
            viewModelScope.launch {
                noteRepository.deleteNoteById(idToDelete)
            }
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
}

