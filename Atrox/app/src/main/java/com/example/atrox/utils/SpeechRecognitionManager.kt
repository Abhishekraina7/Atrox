package com.example.atrox.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Describes the current state of the speech recognition session.
 */
sealed interface SpeechState {
    /** Idle — not listening. */
    data object Idle : SpeechState

    /** The recognizer is initialized and actively listening for speech. */
    data class Listening(val rmsDb: Float = 0f, val partialText: String = "") : SpeechState

    /** Speech was detected; the recognizer is processing audio. */
    data class Processing(val partialText: String = "") : SpeechState

    /** Recognition completed — [text] is the final transcription result. */
    data class Result(val text: String) : SpeechState

    /**
     * An error occurred.
     * [message] is a human-readable description.
     * [isRecoverable] indicates whether the manager can auto-retry (e.g. via online fallback).
     */
    data class Error(val message: String, val isRecoverable: Boolean = false) : SpeechState
}

/**
 * Lifecycle-aware wrapper around Android's [SpeechRecognizer] that provides
 * robust online/offline speech-to-text for the note editor.
 *
 * **Strategy:**
 * 1. First attempt uses `EXTRA_PREFER_OFFLINE = true` for fast, private, on-device recognition.
 * 2. If the offline attempt fails with a network/server error (which paradoxically means the
 *    device *cannot* do it offline), a second attempt is made **without** the offline flag,
 *    letting the system route to cloud servers.
 * 3. If both attempts fail, the error is surfaced to the UI.
 *
 * Usage:
 * ```
 * val manager = SpeechRecognitionManager(context)
 * manager.startListening()       // call from Main thread
 * manager.state.collect { ... }  // observe in ViewModel / Composable
 * manager.stopListening()
 * manager.destroy()              // call when ViewModel clears
 * ```
 */
class SpeechRecognitionManager(private val context: Context) {

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    /** Whether we are currently in the first (offline-preferred) attempt. */
    private var isOfflineAttempt = true

    /** True when the user has explicitly asked us to stop. */
    private var isCanceledByUser = false

    // ── Public API ──────────────────────────────────────────────────

    /**
     * Returns `true` if the device has any speech recognition service available.
     * This does **not** guarantee offline support — only that the system can route
     * recognition requests somewhere.
     */
    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    /**
     * Start listening for speech.  Must be called on the **Main thread**.
     */
    fun startListening() {
        if (!isAvailable()) {
            _state.value = SpeechState.Error(
                message = "Speech recognition is not available on this device.",
                isRecoverable = false
            )
            return
        }

        isCanceledByUser = false
        isOfflineAttempt = true
        beginRecognition(preferOffline = true)
    }

    /**
     * Stop listening and force the recognizer to process the audio it has so far.
     */
    fun finishListening() {
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) { /* already destroyed */ }
        
        val currentState = _state.value
        val partial = if (currentState is SpeechState.Listening) currentState.partialText else ""
        _state.value = SpeechState.Processing(partial)
    }

    /**
     * Stop listening and cancel any in-progress recognition.
     */
    fun cancelListening() {
        isCanceledByUser = true
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) { /* already destroyed */ }
        _state.value = SpeechState.Idle
    }

    /**
     * Release the underlying [SpeechRecognizer].  Call this from
     * `ViewModel.onCleared()` or equivalent.
     */
    fun destroy() {
        isCanceledByUser = true
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) { /* no-op */ }
        speechRecognizer = null
        _state.value = SpeechState.Idle
    }

    // ── Internals ───────────────────────────────────────────────────

    private fun beginRecognition(preferOffline: Boolean) {
        // Tear down any previous instance to avoid leaking listeners
        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer ->
            recognizer.setRecognitionListener(createListener())
            recognizer.startListening(buildIntent(preferOffline))
        }

        _state.value = SpeechState.Listening()
    }

    private fun buildIntent(preferOffline: Boolean): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            // Use the device's current locale
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            // Allow partial (streaming) results so the user gets real-time feedback
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            // Prefer offline when requested
            if (preferOffline) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                    putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
                }
            }
            // Give a generous silence timeout — useful for note-taking where the user
            // may pause to think
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 3000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
        }
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechState.Listening(rmsDb = 0f, partialText = "")
        }

        override fun onBeginningOfSpeech() {
            // Still listening — no state change needed
        }

        override fun onRmsChanged(rmsdB: Float) {
            val currentState = _state.value
            if (currentState is SpeechState.Listening) {
                _state.value = currentState.copy(rmsDb = rmsdB)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) { /* unused */ }

        override fun onEndOfSpeech() {
            val currentState = _state.value
            val partial = if (currentState is SpeechState.Listening) currentState.partialText else ""
            _state.value = SpeechState.Processing(partial)
        }

        override fun onError(error: Int) {
            if (isCanceledByUser) return

            val errorMsg = errorCodeToMessage(error)

            // 13 = ERROR_LANGUAGE_UNAVAILABLE, 12 = ERROR_LANGUAGE_NOT_SUPPORTED, 14 = ERROR_CANNOT_CHECK_SUPPORT
            val isOfflineRelated = error in setOf(
                SpeechRecognizer.ERROR_SERVER,
                SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                12, 13, 14
            )

            if (isOfflineAttempt && isOfflineRelated) {
                // Fallback: retry without the offline flag
                isOfflineAttempt = false
                beginRecognition(preferOffline = false)
                return
            }

            // For ERROR_NO_MATCH or ERROR_SPEECH_TIMEOUT, the user simply didn't
            // say anything — this is recoverable by tapping the mic again.
            val isRecoverable = error in setOf(
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            )

            _state.value = SpeechState.Error(
                message = errorMsg,
                isRecoverable = isRecoverable
            )
        }

        override fun onResults(results: Bundle?) {
            val matches = results
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val bestMatch = matches?.firstOrNull().orEmpty()

            if (bestMatch.isNotEmpty()) {
                _state.value = SpeechState.Result(bestMatch)
            } else {
                _state.value = SpeechState.Error(
                    message = "Could not understand speech. Please try again.",
                    isRecoverable = true
                )
            }
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults
                ?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
                ?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                val currentState = _state.value
                if (currentState is SpeechState.Listening) {
                    _state.value = currentState.copy(partialText = partial)
                } else if (currentState is SpeechState.Processing) {
                    _state.value = currentState.copy(partialText = partial)
                }
            }
        }

        override fun onEvent(eventType: Int, params: Bundle?) { /* unused */ }
    }

    // ── Error-code mapping ──────────────────────────────────────────

    private fun errorCodeToMessage(code: Int): String = when (code) {
        SpeechRecognizer.ERROR_AUDIO ->
            "Audio recording error. Please check your microphone."
        SpeechRecognizer.ERROR_CLIENT ->
            "Client-side error. Please try again."
        SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS ->
            "Microphone permission is required for voice input."
        SpeechRecognizer.ERROR_NETWORK ->
            "Network error. Please check your connection."
        SpeechRecognizer.ERROR_NETWORK_TIMEOUT ->
            "Network timed out. Please try again."
        SpeechRecognizer.ERROR_NO_MATCH ->
            "Didn't catch that. Please try again."
        SpeechRecognizer.ERROR_RECOGNIZER_BUSY ->
            "Recognition service is busy. Please wait a moment."
        SpeechRecognizer.ERROR_SERVER ->
            "Server error. Please try again later."
        SpeechRecognizer.ERROR_SERVER_DISCONNECTED ->
            "Disconnected from recognition service."
        SpeechRecognizer.ERROR_SPEECH_TIMEOUT ->
            "No speech detected. Tap the mic to try again."
        SpeechRecognizer.ERROR_TOO_MANY_REQUESTS ->
            "Too many requests. Please wait a moment."
        12 -> "Language not supported for recognition."
        13 -> "Language pack unavailable. Falling back..."
        14 -> "Cannot check language support."
        else ->
            "Unknown speech recognition error ($code)."
    }
}
