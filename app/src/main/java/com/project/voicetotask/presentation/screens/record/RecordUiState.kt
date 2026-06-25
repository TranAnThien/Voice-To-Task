package com.project.voicetotask.presentation.screens.record

data class RecordUiState(
    val isRecording: Boolean = false,
    val durationText: String = "00:00",
    val title: String = "",

    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val recentMeetingId: String? = null
)
