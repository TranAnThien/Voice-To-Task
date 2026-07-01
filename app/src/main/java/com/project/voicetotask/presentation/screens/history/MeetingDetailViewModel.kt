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
                            summary = m.summary,
                            decisionsText = m.decisionsText,
                            blockersText = m.blockersText,
                            followUpsText = m.followUpsText,
                            audioFilePath = m.audioFilePath,
                            isAudioAvailable = m.audioFilePath
                                ?.let { path -> File(path).exists() }
                                ?: false,
                            transcriptMatchCount = countTranscriptMatches(
                                transcript = m.transcript,
                                query = it.transcriptSearchQuery
                            ),
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
                            isCompleted = task.isCompleted,
                            priority = task.priority,
                            priorityCode = when (task.priority) {
                                "High" -> 2
                                "Medium" -> 1
                                else -> 0
                            },
                            assigneeName = task.assigneeName,
                            dueAt = task.dueAt,
                            reminderTime = task.reminderTime,
                            description = task.description,
                            meetingId = task.meetingId
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
        } else {
            _uiState.update {
                it.copy(
                    isAudioAvailable = false,
                    errorMessage = "Audio file is unavailable."
                )
            }
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

    fun setPlaybackSpeed(speed: Float) {
        audioPlayer.setPlaybackSpeed(speed)
    }

    fun onTranscriptSearchQueryChange(query: String) {
        _uiState.update {
            it.copy(
                transcriptSearchQuery = query,
                transcriptMatchCount = countTranscriptMatches(
                    transcript = it.transcript,
                    query = query
                )
            )
        }
    }

    fun buildTranscriptText(): String {
        val state = _uiState.value
        return state.transcript.ifBlank { "Transcript is unavailable." }
    }

    fun buildSummaryText(): String {
        val state = _uiState.value
        return state.summary.ifBlank { "Meeting summary is unavailable." }
    }

    fun buildMeetingNotesText(): String {
        val state = _uiState.value
        return buildString {
            appendLine(state.title.ifBlank { "Meeting notes" })
            appendLine("Date: ${state.date.ifBlank { "--" }}")
            appendLine("Duration: ${state.duration.ifBlank { "--:--" }}")
            appendLine()
            appendSection("Summary", state.summary)
            appendSection("Decisions", state.decisionsText)
            appendSection("Blockers/Risks", state.blockersText)
            appendSection("Follow-ups", state.followUpsText)
            if (state.tasks.isNotEmpty()) {
                appendLine("Tasks")
                state.tasks.forEachIndexed { index, task ->
                    appendLine(
                        "${index + 1}. ${task.title}" +
                            " | Assignee: ${task.assigneeName.ifBlank { "Me" }}" +
                            " | Priority: ${task.priority.ifBlank { "Medium" }}" +
                            " | Category: ${task.category.ifBlank { "Other" }}"
                    )
                }
                appendLine()
            }
            appendSection("Transcript", state.transcript)
        }.trim()
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, isCompleted)
        }
    }

    private fun formatDuration(millis: Long): String {
        if (millis <= 0L) return "--:--"
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format(Locale.getDefault(), "%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
        }
    }

    private fun countTranscriptMatches(transcript: String, query: String): Int {
        val normalizedQuery = query.trim()
        if (normalizedQuery.isBlank() || transcript.isBlank()) return 0
        return Regex(Regex.escape(normalizedQuery), RegexOption.IGNORE_CASE)
            .findAll(transcript)
            .count()
    }

    private fun StringBuilder.appendSection(title: String, content: String) {
        if (content.isBlank()) return
        appendLine(title)
        appendLine(content.trim())
        appendLine()
    }

    override fun onCleared() {
        super.onCleared()
        audioPlayer.stop()
    }
}
