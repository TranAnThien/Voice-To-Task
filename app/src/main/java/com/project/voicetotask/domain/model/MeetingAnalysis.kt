package com.project.voicetotask.domain.model

data class MeetingAnalysis(
    val correctedTranscript: String,
    val suggestedMeetingTitle: String?,
    val summary: String,
    val keyDecisions: List<String>,
    val blockers: List<String>,
    val followUps: List<String>,
    val tasks: List<Task>
)
