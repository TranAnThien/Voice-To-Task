package com.project.voicetotask.presentation.screens.record

import android.content.Context
import android.net.Uri
import android.webkit.MimeTypeMap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.model.PlayerState
import com.project.voicetotask.domain.model.Resource
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
import javax.inject.Inject

@HiltViewModel
class RecordViewModel @Inject constructor(
    private val audioRecorder: AudioRecorder,
    private val audioPlayer: AudioPlayer,
    private val processMeetingUseCase: ProcessMeetingUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecordUiState())
    val uiState: StateFlow<RecordUiState> = _uiState.asStateFlow()

    private var timerJob: Job? = null
    private var secondsElapsed = 0L

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

    fun startRecording(context: Context) {
        if (_uiState.value.isProcessing) {
            return
        }

        try {
            // Create a temporary file in context.cacheDir
            val file = File(context.cacheDir, "recording_${System.currentTimeMillis()}.m4a")
            currentAudioFile = file

            val success = audioRecorder.startRecording(file)
            if (success) {
                startTimer()

                _uiState.update {
                    it.copy(
                        isRecording = true,
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

        val audioFile = currentAudioFile
        if (audioFile == null || !audioFile.exists()) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isProcessing = false,
                    errorMessage = "Audio file not found."
                )
            }
            return
        }

        if (audioFile.length() <= 0L) {
            _uiState.update {
                it.copy(
                    isRecording = false,
                    isProcessing = false,
                    errorMessage = "Recording is too short. Please try again."
                )
            }
            return
        }

        android.util.Log.d("RecordViewModel", "Recorded file size: ${audioFile.length()} bytes")

        processAudioFile(audioFile)
    }

    fun processUploadedAudio(context: Context, uri: Uri) {
        if (_uiState.value.isRecording || _uiState.value.isProcessing) {
            return
        }

        _uiState.update {
            it.copy(
                isProcessing = true,
                errorMessage = null,
                recentMeetingId = null
            )
        }

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val mimeType = context.contentResolver.getType(uri)
                if (mimeType != null && !mimeType.startsWith("audio/")) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Please choose an audio file."
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
                            errorMessage = "Could not open selected audio file."
                        )
                    }
                    return@launch
                }

                if (!copiedFile.exists() || copiedFile.length() <= 0L) {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            errorMessage = "Selected audio file is empty."
                        )
                    }
                    return@launch
                }

                processAudioFile(copiedFile)
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isProcessing = false,
                        errorMessage = "Failed to process selected audio: ${e.message}"
                    )
                }
            }
        }
    }

    private fun processAudioFile(audioFile: File) {
        _uiState.update {
            it.copy(
                isRecording = false,
                isProcessing = true,
                recentMeetingId = null,
                errorMessage = null
            )
        }

        // Process Meeting using IO Dispatcher to safely handle Network and Database I/O requests
        viewModelScope.launch(Dispatchers.IO) {
            when (val result = processMeetingUseCase(audioFile)) {
                is Resource.Success -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
                            recentMeetingId = result.data.first.id
                        )
                    }
                }
                is Resource.Error -> {
                    _uiState.update {
                        it.copy(
                            isProcessing = false,
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

    private fun startTimer() {
        secondsElapsed = 0L
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                secondsElapsed++
                _uiState.update { it.copy(durationText = formatTime(secondsElapsed)) }
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
}
