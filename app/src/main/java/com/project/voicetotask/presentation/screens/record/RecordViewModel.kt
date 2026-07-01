package com.project.voicetotask.presentation.screens.record

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.model.PlayerState
import com.project.voicetotask.domain.model.Resource
import com.project.voicetotask.domain.model.AiPromptProfile
import com.project.voicetotask.domain.repository.AudioDurationReader
import com.project.voicetotask.domain.repository.AudioPlayer
import com.project.voicetotask.domain.repository.AudioRecorder
import com.project.voicetotask.domain.usecase.ProcessMeetingUseCase
import com.project.voicetotask.presentation.screens.record.RecordUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import java.io.File
import kotlin.math.max
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val audioDurationReader: AudioDurationReader,
    private val processMeetingUseCase: ProcessMeetingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var secondsElapsed = 0L
    private var peakAmplitude = 0

    // Expose amplitude flow for UI visualization mapping directly from Domain Layer
    val amplitudeFlow: StateFlow<Int> = audioRecorder.getAmplitudeFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = 0
        )

    // Expose player state flow for Audio playback UI exactly matching the Domain Layer
    val playerStateFlow: StateFlow<PlayerState> = audioPlayer.getPlayerStateFlow()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = PlayerState()
        )

    private var currentAudioFile: File? = null
    private var retryAudioFile: File? = null
    private var retryFallbackDurationMillis: Long = 0L
    private var retryPromptProfile: AiPromptProfile = AiPromptProfile.default

    fun onPromptProfileSelected(profile: AiPromptProfile) {
        if (_uiState.value.isRecording || _uiState.value.isProcessing) return
        _uiState.update {
            it.copy(
                selectedPromptProfile = profile,
                errorMessage = null
            )
        }
    }

    fun startRecording(context: Context) {
        if (_uiState.value.isProcessing) {
            return
        }

        try {
            clearRetryAudio(deleteFile = true)
            peakAmplitude = 0
            // Create a temporary file in context.cacheDir
            val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
            currentAudioFile = file

            val success = audioRecorder.startRecording(file)
            if (success) {
                startTimer()

                _uiState.update {
                    it.copy(
                        isRecording = true,
                        isPaused = false,
                        qualityWarning = null,
                        canRetryProcessing = false,
                        canDeleteCurrentAudio = false,
                        errorMessage = null,
                        recentMeetingId = null
                    )
                }
            } else {
                _uiState.update {
                    it.copy(errorMessage = "Failed to initialize microphone. Check permissions.")
                }
            }
        } catch (e: Exception) {
            _uiState.update {
                it.copy(errorMessage = "Failed to start recording: ${e.message}")
            }
        }
    }

    fun stopRecordingAndProcess() {
        if (_uiState.value.isProcessing) {
            return
        }

        audioRecorder.stopRecording()
        stopTimer()
        val fallbackDurationMillis = secondsElapsed * 1000L

        val audioFile = currentAudioFile
        if (audioFile == null || !audioFile.exists()) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isProcessing = false,
                    canRetryProcessing = false,
                    canDeleteCurrentAudio = false,
                    errorMessage = "Audio file not found."
                )
            }
            return
        }

        val validation = validateAudioFile(
            audioFile = audioFile,
            fallbackDurationMillis = fallbackDurationMillis,
            requireVoiceSignal = true
        )
        if (validation is AudioValidation.Invalid) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isProcessing = false,
                    canRetryProcessing = false,
                    canDeleteCurrentAudio = true,
                    errorMessage = validation.message
                )
            }
            return
        }

        processAudioFile(
            audioFile = audioFile,
            fallbackDurationMillis = fallbackDurationMillis
        )
    }

    fun togglePauseRecording() {
        val state = _uiState.value
        if (!state.isRecording || state.isProcessing) return

        if (state.isPaused) {
            audioRecorder.resumeRecording()
            resumeTimer()
            _uiState.update { it.copy(isPaused = false) }
        } else {
            audioRecorder.pauseRecording()
            stopTimer()
            _uiState.update { it.copy(isPaused = true) }
        }
    }

    fun cancelRecording() {
        if (_uiState.value.isProcessing) return

        audioRecorder.stopRecording()
        stopTimer()
        currentAudioFile?.takeIf { it.exists() }?.delete()
        clearRetryAudio(deleteFile = true)
        secondsElapsed = 0L
        peakAmplitude = 0
        _uiState.update {
            it.copy(
                isRecording = false,
                isPaused = false,
                durationText = "00:00",
                isProcessing = false,
                qualityWarning = null,
                canRetryProcessing = false,
                canDeleteCurrentAudio = false,
                errorMessage = null,
                recentMeetingId = null
            )
        }
    }

    fun processUploadedAudio(context: Context, uri: Uri) {
        if (_uiState.value.isRecording || _uiState.value.isProcessing) {
            return
        }

        _uiState.update {
            it.copy(
                isProcessing = true,
                qualityWarning = null,
                canRetryProcessing = false,
                canDeleteCurrentAudio = false,
                errorMessage = null,
                recentMeetingId = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType == null || !mimeType.startsWith("audio/")) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            canRetryProcessing = false,
                            canDeleteCurrentAudio = false,
                            errorMessage = "Please choose a valid audio file."
                        )
                    }
                    return@launch
                }

                val extension = MimeTypeMap.getSingleton()
                    .getExtensionFromMimeType(mimeType)
                    ?: "m4a"
                val copiedFile = File(context.cacheDir, "uploaded_${System.currentTimeMillis()}.$extension")

                context.contentResolver.openInputStream(uri)?.use { input ->
                    copiedFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: run {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            canRetryProcessing = false,
                            canDeleteCurrentAudio = false,
                            errorMessage = "Could not open selected audio file."
                        )
                    }
                    return@launch
                }

                val validation = validateAudioFile(copiedFile)
                if (validation is AudioValidation.Invalid) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            canRetryProcessing = false,
                            canDeleteCurrentAudio = true,
                            errorMessage = validation.message
                        )
                    }
                    currentAudioFile = copiedFile
                    return@launch
                }

                currentAudioFile = copiedFile
                processAudioFile(copiedFile)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        canRetryProcessing = false,
                        canDeleteCurrentAudio = false,
                        errorMessage = "Failed to process selected audio: ${e.message}"
                    )
                }
            }
        }
    }

    private fun processAudioFile(
        audioFile: File,
        fallbackDurationMillis: Long = 0L,
        promptProfile: AiPromptProfile = _uiState.value.selectedPromptProfile
    ) {
        val validation = validateAudioFile(
            audioFile = audioFile,
            fallbackDurationMillis = fallbackDurationMillis,
            requireVoiceSignal = false
        )
        if (validation is AudioValidation.Invalid) {
            retryAudioFile = audioFile
            retryFallbackDurationMillis = fallbackDurationMillis
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isPaused = false,
                    isProcessing = false,
                    canRetryProcessing = false,
                    canDeleteCurrentAudio = true,
                    errorMessage = validation.message
                )
            }
            return
        }

        val validAudio = validation as AudioValidation.Valid
        val selectedProfile = promptProfile
        retryAudioFile = audioFile
        retryFallbackDurationMillis = fallbackDurationMillis
        retryPromptProfile = selectedProfile
        _uiState.update {
            it.copy(
                isRecording = false,
                isPaused = false,
                isProcessing = true,
                recentMeetingId = null,
                canRetryProcessing = false,
                canDeleteCurrentAudio = false,
                qualityWarning = validAudio.warning,
                errorMessage = null
            )
        }

        // Process Meeting using IO Dispatcher to safely handle Network and Database I/O requests
        viewModelScope.launch(Dispatchers.IO) {
            val durationMillis = audioDurationReader.getDurationMillis(audioFile)
                ?: fallbackDurationMillis
            when (
                val result = processMeetingUseCase(
                    audioFile = audioFile,
                    durationMillis = durationMillis,
                    profile = selectedProfile
                )
            ) {
                is Resource.Success -> {
                    clearRetryAudio(deleteFile = false)
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            canRetryProcessing = false,
                            canDeleteCurrentAudio = false,
                            qualityWarning = null,
                            recentMeetingId = result.data.first.id
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            canRetryProcessing = true,
                            canDeleteCurrentAudio = true,
                            errorMessage = result.message
                        )
                    }
                }
                Resource.Loading -> {
                    // Ignored since we handle boolean state locally
                }
            }
        }
    }

    fun retryProcessing() {
        if (_uiState.value.isProcessing || _uiState.value.isRecording) return
        val file = retryAudioFile ?: currentAudioFile
        if (file == null || !file.exists()) {
            _uiState.update {
                it.copy(
                    canRetryProcessing = false,
                    canDeleteCurrentAudio = false,
                    errorMessage = "Audio file is no longer available. Please record or upload again."
                )
            }
            return
        }
        processAudioFile(
            audioFile = file,
            fallbackDurationMillis = retryFallbackDurationMillis,
            promptProfile = retryPromptProfile
        )
    }

    fun deleteCurrentAudio() {
        if (_uiState.value.isProcessing) return
        currentAudioFile?.takeIf { it.exists() }?.delete()
        retryAudioFile?.takeIf { it.exists() }?.delete()
        clearRetryAudio(deleteFile = false)
        secondsElapsed = 0L
        peakAmplitude = 0
        _uiState.update {
            it.copy(
                isRecording = false,
                isPaused = false,
                isProcessing = false,
                durationText = "00:00",
                qualityWarning = null,
                canRetryProcessing = false,
                canDeleteCurrentAudio = false,
                errorMessage = null,
                recentMeetingId = null
            )
        }
    }

    fun playAudio(file: File) {
        audioPlayer.playFile(file)
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun resumeAudio() {
        audioPlayer.resume()
    }

    fun stopAudio() {
        audioPlayer.stop()
    }

    fun seekAudio(position: Long) {
        audioPlayer.seekTo(position)
    }

    fun clearError() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun consumeRecentMeeting() {
        _uiState.update { it.copy(recentMeetingId = null) }
    }

    private fun startTimer() {
        secondsElapsed = 0L
        resumeTimer()
    }

    private fun resumeTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                secondsElapsed++
                peakAmplitude = max(peakAmplitude, amplitudeFlow.value)
                val warning = if (
                    _uiState.value.isRecording &&
                    !_uiState.value.isPaused &&
                    secondsElapsed >= 3 &&
                    peakAmplitude in 1 until MIN_PEAK_AMPLITUDE
                ) {
                    "Audio level looks low. Move closer to the microphone."
                } else {
                    null
                }
                _uiState.update {
                    it.copy(
                        durationText = formatTime(secondsElapsed),
                        qualityWarning = warning
                    )
                }
            }
        }
    }

    private fun stopTimer() {
        timerJob?.cancel()
        timerJob = null
    }

    private fun formatTime(seconds: Long): String {
        val mins = seconds / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", mins, secs)
    }

    private fun validateAudioFile(
        audioFile: File,
        fallbackDurationMillis: Long = 0L,
        requireVoiceSignal: Boolean = false
    ): AudioValidation {
        if (!audioFile.exists() || !audioFile.canRead()) {
            return AudioValidation.Invalid("Audio file is missing or cannot be read.")
        }
        if (audioFile.length() < MIN_AUDIO_BYTES) {
            return AudioValidation.Invalid(
                "Audio is too short or empty. Please record at least 5 seconds with clear speech."
            )
        }

        val durationMillis = audioDurationReader.getDurationMillis(audioFile)
            ?: fallbackDurationMillis.takeIf { it > 0L }
        if (durationMillis != null && durationMillis < MIN_AUDIO_DURATION_MILLIS) {
            return AudioValidation.Invalid(
                "Audio is too short or has no clear speech. Please record at least 5 seconds."
            )
        }

        if (requireVoiceSignal && durationMillis != null && peakAmplitude < MIN_PEAK_AMPLITUDE) {
            return AudioValidation.Invalid(
                "Audio level is too low or silent. Please record again closer to the microphone."
            )
        }

        val warning = if (durationMillis == null) {
            "Could not read audio duration. Processing will continue based on file size."
        } else {
            null
        }
        return AudioValidation.Valid(warning)
    }

    private fun clearRetryAudio(deleteFile: Boolean) {
        if (deleteFile) {
            retryAudioFile?.takeIf { it.exists() }?.delete()
            currentAudioFile?.takeIf { it.exists() }?.delete()
        }
        retryAudioFile = null
        retryFallbackDurationMillis = 0L
        currentAudioFile = null
    }

    private sealed interface AudioValidation {
        data class Valid(val warning: String? = null) : AudioValidation
        data class Invalid(val message: String) : AudioValidation
    }

    private companion object {
        const val MIN_AUDIO_DURATION_MILLIS = 5_000L
        const val MIN_AUDIO_BYTES = 16 * 1024L
        const val MIN_PEAK_AMPLITUDE = 500
    }
}
