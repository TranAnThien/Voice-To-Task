package com.project.voicetotask.domain.model

data class Task(
    val id: String,
    val title: String,
    val description: String,
    val category: String,
    val priority: String,
    val isCompleted: Boolean,
    val reminderTime: Long?,
    val meetingId: String?,
    val assigneeName: String = DEFAULT_ASSIGNEE,
    val dueAt: Long? = null
)

const val DEFAULT_ASSIGNEE = "Me"
