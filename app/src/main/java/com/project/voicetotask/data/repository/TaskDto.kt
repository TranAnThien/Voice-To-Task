package com.project.voicetotask.data.repository

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskDto(
    val title: String?,
    val description: String?,
    val assigneeName: String?,
    val deadlineText: String?,
    val deadlineIso: String?,
    val category: String?,
    val priority: String?,
    val isCompleted: Boolean = false
)

@JsonClass(generateAdapter = true)
data class MeetingAnalysisDto(
    val suggestedMeetingTitle: String?,
    val correctedTranscript: String?,
    val summary: String?,
    val keyDecisions: List<String>?,
    val blockers: List<String>?,
    val followUps: List<String>?,
    val tasks: List<TaskDto>?
)
