package com.project.voicetotask.presentation.screens.history

data class MeetingModel(
    val id: String = "",
    val title: String = "",
    val date: String = "",
    val duration: String = "",
    val taskCount: Int = 0,
    val summaryPreview: String = "",
    val badges: List<String> = emptyList(),
    val assigneePreview: String = ""
)

data class HistoryUiState(
    val meetings: List<MeetingModel> = emptyList(),
    val searchQuery: String = "",
    val activeFilter: HistoryFilter = HistoryFilter.ALL,
    val emptyMessage: String = HistoryFilter.ALL.emptyMessage
)

enum class HistoryFilter(
    val label: String,
    val emptyMessage: String
) {
    ALL("All", "No meetings yet"),
    THIS_WEEK("This Week", "No meetings this week"),
    THIS_MONTH("This Month", "No meetings this month"),
    HAS_TASKS("Has Tasks", "No meetings with tasks"),
    NO_TASKS("No Tasks", "No meetings without tasks"),
    HAS_BLOCKERS("Has Blockers", "No meetings with blockers"),
    HAS_DECISIONS("Has Decisions", "No meetings with decisions"),
    HAS_FOLLOW_UPS("Has Follow-ups", "No meetings with follow-ups"),
    HAS_OVERDUE_TASKS("Has Overdue Tasks", "No meetings with overdue tasks"),
    ASSIGNED_TO_ME("Assigned To Me", "No meetings with tasks assigned to you")
}
