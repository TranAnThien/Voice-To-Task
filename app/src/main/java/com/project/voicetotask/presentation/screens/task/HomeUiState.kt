package com.project.voicetotask.presentation.screens.task

data class TaskModel(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val isCompleted: Boolean = false,
    val priorityCode: Int = 1,
    val priority: String = "Medium",
    val assigneeName: String = "Me",
    val dueAt: Long? = null,
    val reminderTime: Long? = null,
    val description: String = "",
    val meetingId: String? = null,
    val deadlineStatus: TaskDeadlineStatus = TaskDeadlineStatus.NONE
)

enum class TaskDeadlineStatus {
    NONE,
    OVERDUE,
    TODAY,
    UPCOMING
}

enum class TaskDashboardFilter(
    val label: String,
    val emptyMessage: String
) {
    ALL("All", "No tasks yet"),
    MY_TASKS("My Tasks", "No tasks assigned to you"),
    TODAY("Today", "No tasks due today"),
    OVERDUE("Overdue", "No overdue tasks"),
    UPCOMING("Upcoming", "No upcoming tasks"),
    UNASSIGNED("Unassigned", "No unassigned tasks"),
    HIGH_PRIORITY("High Priority", "No high-priority tasks"),
    FROM_MEETINGS("From Meetings", "No tasks from meetings")
}

data class HomeUiState(
    val userName: String = "Thien",
    val pendingTaskCount: Int = 0,
    val completedTaskCount: Int = 0,
    val overdueTaskCount: Int = 0,
    val upcomingTaskCount: Int = 0,
    val myTaskCount: Int = 0,
    val unassignedTaskCount: Int = 0,
    val highPriorityTaskCount: Int = 0,
    val meetingLinkedTaskCount: Int = 0,
    val recordedMeetingsCount: Int = 0,
    val searchQuery: String = "",
    val activeFilter: TaskDashboardFilter = TaskDashboardFilter.ALL,
    val recentTasks: List<TaskModel> = emptyList(),
    val emptyMessage: String = TaskDashboardFilter.ALL.emptyMessage,
    val isLoading: Boolean = false,
    val taskPendingDelete: TaskModel? = null
)
