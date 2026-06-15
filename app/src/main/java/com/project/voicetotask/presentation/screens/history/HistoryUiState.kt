package com.project.voicetotask.presentation.screens.history

data class MeetingModel(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val duration: String = "",
    val taskCount: Int = 0
)

data class HistoryUiState(
    val meetings: List<MeetingModel> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: String = "This week"
)
