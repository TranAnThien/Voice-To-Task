package com.project.voicetotask.presentation.screens.task

data class TaskDetailUiState(
    val taskId: String = "",
    val title: String = "",
    val notes: String = "",
    val category: String = "Work",
    val priority: String = "Medium",
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false
)
