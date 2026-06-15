package com.project.voicetotask.presentation.screens.task

data class TaskModel(
    val id: String = "",
    val title: String = "",
    val category: String = "",
    val isCompleted: Boolean = false,
    val priorityCode: Int = 1 // 0: Low, 1: Medium, 2: High
)

data class HomeUiState(
    val userName: String = "Thien",
    val tasksToDoTodayCount: Int = 5,
    val recordedMeetingsCount: Int = 2,
    val searchQuery: String = "",
    val activeCategory: String = "All",
    val recentTasks: List<TaskModel> = emptyList(),
    val isLoading: Boolean = false
)
