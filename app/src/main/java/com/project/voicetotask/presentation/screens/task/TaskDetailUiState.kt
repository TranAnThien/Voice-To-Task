package com.project.voicetotask.presentation.screens.task

data class TaskDetailUiState(
    val taskId: String = "",
    val title: String = "",
    val notes: String = "",
    val category: String = "Work",
    val priority: String = "Medium",
    val isCompleted: Boolean = false,
    val isLoading: Boolean = false,
    val isEditMode: Boolean = false,
    val canSave: Boolean = false,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)
