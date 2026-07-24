package com.example.atrox.utils

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch


sealed interface SpeechState {
    //idle — not listening.
    data object Idle : SpeechState

    // The recognizer is initialized and actively listening for speech.
    data class Listening(val rmsDb: Float = 0f, val partialText: String = "") : SpeechState

    //Speech was detected; the recognizer is processing audio.
    data class Processing(val partialText: String = "") : SpeechState

    // Recognition completed — [text] is the final transcription result.
    data class Result(val text: String) : SpeechState

    /**
     * An error occurred.
     * [message] is a human-readable description.
     * [isRecoverable] indicates whether the manager can auto-retry (e.g. via online fallback).
     */
    data class Error(val message: String, val isRecoverable: Boolean = false) : SpeechState
}

class SpeechRecognitionManager(private val context: Context) {

    private val _state = MutableStateFlow<SpeechState>(SpeechState.Idle)
    val state: StateFlow<SpeechState> = _state.asStateFlow()

    private var speechRecognizer: SpeechRecognizer? = null

    // Whether we are currently in the first (offline-preferred) attempt.
    private var isOfflineAttempt = true

    // True when the user has explicitly canceled.
    private var isCanceledByUser = false
    
    // True when the user manually stopped recording to get the result.
    private var isFinishing = false

    private val scope = CoroutineScope(Dispatchers.Main + SupervisorJob())
    private var silenceTimeoutJob: Job? = null
    
    private var accumulatedText = ""
    private var currentPartialText = ""

    // ── Public API ──────────────────────────────────────────────────

    fun isAvailable(): Boolean = SpeechRecognizer.isRecognitionAvailable(context)

    fun startListening() {
        if (!isAvailable()) {
            _state.value = SpeechState.Error(
                message = "Speech recognition is not available on this device.",
                isRecoverable = false
            )
            return
        }

        isCanceledByUser = false
        isFinishing = false
        isOfflineAttempt = true
        accumulatedText = ""
        currentPartialText = ""
        beginRecognition(preferOffline = true)
        resetSilenceWatchdog()
    }

    fun finishListening() {
        isCanceledByUser = true
        isFinishing = true
        silenceTimeoutJob?.cancel()
        try {
            speechRecognizer?.stopListening()
        } catch (_: Exception) { /* already destroyed */ }
        
        val fullText = getFullText(currentPartialText)
        _state.value = SpeechState.Processing(fullText)
    }

    fun cancelListening() {
        isCanceledByUser = true
        isFinishing = false
        silenceTimeoutJob?.cancel()
        try {
            speechRecognizer?.stopListening()
            speechRecognizer?.cancel()
        } catch (_: Exception) { /* already destroyed */ }
        _state.value = SpeechState.Idle
    }

    fun destroy() {
        isCanceledByUser = true
        scope.cancel()
        try {
            speechRecognizer?.destroy()
        } catch (_: Exception) { /* no-op */ }
        speechRecognizer = null
        _state.value = SpeechState.Idle
    }

    // ── Internals ───────────────────────────────────────────────────

    private fun getFullText(partial: String): String {
        return (accumulatedText + partial).trim()
    }

    private fun resetSilenceWatchdog() {
        silenceTimeoutJob?.cancel()
        silenceTimeoutJob = scope.launch {
            delay(10000L) // 10 seconds of absolute silence
            if (!isCanceledByUser) {
                // Time's up! Automatically stop and finalize.
                finishListening()
            }
        }
    }

    private fun beginRecognition(preferOffline: Boolean) {
        speechRecognizer?.destroy()

        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(context).also { recognizer ->
            recognizer.setRecognitionListener(createListener())
            recognizer.startListening(buildIntent(preferOffline))
        }

        _state.value = SpeechState.Listening(rmsDb = 0f, partialText = getFullText(currentPartialText))
    }

    private fun buildIntent(preferOffline: Boolean): Intent {
        return Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
            if (preferOffline && android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
                putExtra(RecognizerIntent.EXTRA_PREFER_OFFLINE, true)
            }
            // Use longer internal timeouts to reduce the frequency of automatic restarts
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_POSSIBLY_COMPLETE_SILENCE_LENGTH_MILLIS, 5000L)
            putExtra(RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS, 8000L)
        }
    }

    private fun createListener(): RecognitionListener = object : RecognitionListener {

        override fun onReadyForSpeech(params: Bundle?) {
            _state.value = SpeechState.Listening(rmsDb = 0f, partialText = getFullText(currentPartialText))
            resetSilenceWatchdog()
        }

        override fun onBeginningOfSpeech() {
            resetSilenceWatchdog()
        }

        override fun onRmsChanged(rmsdB: Float) {
            val currentState = _state.value
            if (currentState is SpeechState.Listening) {
                _state.value = currentState.copy(rmsDb = rmsdB)
            }
        }

        override fun onBufferReceived(buffer: ByteArray?) { /* unused */ }

        override fun onEndOfSpeech() {
            val partial = getFullText(currentPartialText)
            _state.value = SpeechState.Processing(partial)
        }

        override fun onError(error: Int) {
            if (isCanceledByUser) {
                if (isFinishing) {
                    val fallback = getFullText(currentPartialText)
                    if (fallback.isNotEmpty()) {
                        _state.value = SpeechState.Result(fallback)
                    } else {
                        _state.value = SpeechState.Error("Could not understand speech.", true)
                    }
                }
                return
            }

            val isOfflineRelated = error in setOf(
                SpeechRecognizer.ERROR_SERVER,
                SpeechRecognizer.ERROR_SERVER_DISCONNECTED,
                SpeechRecognizer.ERROR_NETWORK,
                SpeechRecognizer.ERROR_NETWORK_TIMEOUT,
                12, 13, 14
            )

            if (isOfflineAttempt && isOfflineRelated) {
                isOfflineAttempt = false
                beginRecognition(preferOffline = false)
                return
            }

            val isRecoverable = error in setOf(
                SpeechRecognizer.ERROR_NO_MATCH,
                SpeechRecognizer.ERROR_SPEECH_TIMEOUT
            )

            if (isRecoverable) {
                // Continuous Mode: OS recognizer timed out, but our 10s watchdog hasn't. Restart!
                beginRecognition(isOfflineAttempt)
                return
            }

            _state.value = SpeechState.Error(errorCodeToMessage(error), isRecoverable = false)
        }

        override fun onResults(results: Bundle?) {
            val matches = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
            val bestMatch = matches?.firstOrNull().orEmpty()

            if (isCanceledByUser) {
                if (isFinishing) {
                    val finalResult = getFullText(bestMatch)
                    if (finalResult.isNotEmpty()) {
                        _state.value = SpeechState.Result(finalResult)
                    } else {
                        val fallback = getFullText(currentPartialText)
                        if (fallback.isNotEmpty()) {
                            _state.value = SpeechState.Result(fallback)
                        } else {
                            _state.value = SpeechState.Error("Could not understand speech.", true)
                        }
                    }
                }
                return
            }

            // Continuous Mode: Append and Restart!
            if (bestMatch.isNotBlank()) {
                accumulatedText = getFullText(bestMatch) + " "
                currentPartialText = ""
            }
            
            beginRecognition(isOfflineAttempt)
        }

        override fun onPartialResults(partialResults: Bundle?) {
            val partial = partialResults?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)?.firstOrNull()
            if (!partial.isNullOrBlank()) {
                currentPartialText = partial
                resetSilenceWatchdog()
                val full = getFullText(currentPartialText)
                
                val currentState = _state.value
                if (currentState is SpeechState.Listening) {
                    _state.value = currentState.copy(partialText = full)
                } else if (currentState is SpeechState.Processing) {
                    _state.value = currentState.copy(partialText = full)
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
