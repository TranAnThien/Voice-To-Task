package com.project.voicetotask.presentation.screens.record

import com.project.voicetotask.domain.model.AiPromptProfile

data class RecordUiState(
    val isRecording: Boolean = false,
    val isPaused: Boolean = false,
    val durationText: String = "00:00",
    val title: String = "",

    val isProcessing: Boolean = false,
    val errorMessage: String? = null,
    val qualityWarning: String? = null,
    val canRetryProcessing: Boolean = false,
    val canDeleteCurrentAudio: Boolean = false,
    val selectedPromptProfile: AiPromptProfile = AiPromptProfile.default,
    val availablePromptProfiles: List<AiPromptProfile> = AiPromptProfile.entries,
    val recentMeetingId: String? = null
)
