package com.project.voicetotask.presentation.screens.history

import com.project.voicetotask.domain.model.PlayerState
import com.project.voicetotask.presentation.screens.task.TaskModel

data class MeetingDetailUiState(
    val title: String = "",
    val date: String = "",
    val duration: String = "",
    val transcript: String = "",
    val summary: String = "",
    val decisionsText: String = "",
    val blockersText: String = "",
    val followUpsText: String = "",
    val audioFilePath: String? = null,
    val isAudioAvailable: Boolean = false,
    val tasks: List<TaskModel> = emptyList(),
    val playerState: PlayerState = PlayerState(),
    val transcriptSearchQuery: String = "",
    val transcriptMatchCount: Int = 0,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
