package com.project.voicetotask.presentation.screens.history

import com.project.voicetotask.domain.model.PlayerState
import com.project.voicetotask.presentation.screens.task.TaskModel

data class MeetingDetailUiState(
    val title: String = "",
    val date: String = "",
    val duration: String = "",
    val transcript: String = "",
    val audioFilePath: String? = null,
    val tasks: List<TaskModel> = emptyList(),
    val playerState: PlayerState = PlayerState(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)
