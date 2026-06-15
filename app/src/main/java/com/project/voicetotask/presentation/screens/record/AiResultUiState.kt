package com.project.voicetotask.presentation.screens.record

import com.project.voicetotask.presentation.screens.task.TaskModel

data class AiResultUiState(
    val summary: String = "",
    val tasks: List<TaskModel> = emptyList(),
    val isLoading: Boolean = false
)
