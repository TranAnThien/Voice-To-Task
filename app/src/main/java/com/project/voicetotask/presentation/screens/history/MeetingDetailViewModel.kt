package com.project.voicetotask.presentation.screens.history

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.repository.AudioPlayer
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import com.project.voicetotask.presentation.screens.task.TaskModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class MeetingDetailViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val taskRepository: TaskRepository,
    private val audioPlayer: AudioPlayer,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val meetingId: String? = savedStateHandle["meetingId"]
    private val dateFormatter = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())

    private val _uiState = MutableStateFlow(MeetingDetailUiState())
    val uiState: StateFlow<MeetingDetailUiState> = _uiState.asStateFlow()

    init {
        meetingId?.let { id ->
            loadMeeting(id)
            observePlayerState()
        }
    }

    private fun loadMeeting(id: String) {
        _uiState.update { it.copy(isLoading = true) }
        viewModelScope.launch {
            meetingRepository.getMeetingById(id).collect { meeting ->
                meeting?.let { m ->
                    _uiState.update { 
                        it.copy(
                            title = m.title,
                            date = dateFormatter.format(Date(m.date)),
                            duration = formatDuration(m.duration),
                            transcript = m.transcript,
                            audioFilePath = m.audioFilePath,
                            isLoading = false
                        )
                    }
                }
            }
        }
        viewModelScope.launch {
            taskRepository.getTasksForMeeting(id).collect { tasks ->
                _uiState.update { state ->
                    state.copy(tasks = tasks.map { task ->
                        TaskModel(
                            id = task.id,
                            title = task.title,
                            category = task.category,
                            isCompleted = task.isCompleted
                        )
                    })
                }
            }
        }
    }

    private fun observePlayerState() {
        viewModelScope.launch {
            audioPlayer.getPlayerStateFlow().collect { playerState ->
                _uiState.update { it.copy(playerState = playerState) }
            }
        }
    }

    fun playAudio() {
        val path = _uiState.value.audioFilePath ?: return
        val file = File(path)
        if (file.exists()) {
            audioPlayer.playFile(file)
        }
    }

    fun pauseAudio() {
        audioPlayer.pause()
    }

    fun resumeAudio() {
        audioPlayer.resume()
    }

    fun seekTo(position: Long) {
        audioPlayer.seekTo(position)
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, isCompleted)
        }
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
