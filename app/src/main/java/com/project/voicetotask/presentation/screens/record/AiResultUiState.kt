package com.project.voicetotask.presentation.screens.record

import com.project.voicetotask.presentation.screens.task.TaskModel

data class AiResultUiState(
    val transcript: String = "",
    val meetingSummary: String = "",
    val decisionsText: String = "",
    val blockersText: String = "",
    val followUpsText: String = "",
    val tasks: List<TaskModel> = emptyList(),
    val editingTask: AiReviewTaskDraft? = null,
    val taskPendingDelete: TaskModel? = null,
    val isLoading: Boolean = false,
    val isSaving: Boolean = false,
    val showExitDialog: Boolean = false,
    val isFinished: Boolean = false,
    val errorMessage: String? = null
)

data class AiReviewTaskDraft(
    val id: String? = null,
    val title: String = "",
    val description: String = "",
    val assigneeName: String = "Me",
    val dueAt: Long? = null,
    val category: String = "Other",
    val priority: String = "Medium",
    val isCompleted: Boolean = false,
    val reminderTime: Long? = null
) {
    val isNew: Boolean = id == null
    val canSave: Boolean = title.isNotBlank()
}
