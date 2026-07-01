package com.project.voicetotask.presentation.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.Calendar
import java.util.Locale
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val meetingRepository: MeetingRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(TaskDashboardFilter.ALL)
    private val dashboardControls = combine(
        _searchQuery,
        _activeFilter
    ) { query, filter -> query to filter }
    private val currentTime = flow {
        while (true) {
            emit(System.currentTimeMillis())
            delay(DASHBOARD_REFRESH_INTERVAL_MILLIS)
        }
    }

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepository.getAllTasks(),
        meetingRepository.getAllMeetings(),
        dashboardControls,
        currentTime
    ) { tasks, meetings, controls, now ->
        val (query, filter) = controls
        val dayBounds = dayBounds(now)
        val upcomingEnd = dayBounds.endExclusive + UPCOMING_WINDOW_MILLIS

        val visibleTasks = tasks
            .asSequence()
            .filter { it.matchesSearch(query) }
            .filter { it.matchesFilter(filter, now, dayBounds, upcomingEnd) }
            .sortedWith(taskIntelligenceComparator(now, dayBounds, upcomingEnd))
            .map { it.toTaskModel(now, dayBounds, upcomingEnd) }
            .toList()

        HomeUiState(
            userName = "User",
            pendingTaskCount = tasks.count { !it.isCompleted },
            completedTaskCount = tasks.count { it.isCompleted },
            overdueTaskCount = tasks.count { it.isOverdue(now) },
            upcomingTaskCount = tasks.count {
                it.isUpcoming(dayBounds, upcomingEnd)
            },
            myTaskCount = tasks.count { it.isAssignedToMe() },
            unassignedTaskCount = tasks.count { it.isUnassigned() },
            highPriorityTaskCount = tasks.count {
                !it.isCompleted && it.priority.equals("High", ignoreCase = true)
            },
            meetingLinkedTaskCount = tasks.count { it.meetingId != null },
            recordedMeetingsCount = meetings.size,
            searchQuery = query,
            activeFilter = filter,
            recentTasks = visibleTasks,
            emptyMessage = if (query.isNotBlank()) {
                "No tasks match \"$query\""
            } else {
                filter.emptyMessage
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(filterLabel: String) {
        _activeFilter.value = TaskDashboardFilter.entries
            .firstOrNull { it.label == filterLabel }
            ?: TaskDashboardFilter.ALL
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, isCompleted)
        }
    }

    fun deleteTask(taskId: String) {
        viewModelScope.launch {
            taskRepository.deleteTaskById(taskId)
        }
    }

    private fun Task.toTaskModel(
        now: Long,
        dayBounds: DayBounds,
        upcomingEnd: Long
    ): TaskModel {
        return TaskModel(
            id = id,
            title = title,
            category = category,
            isCompleted = isCompleted,
            priorityCode = priorityRank(priority),
            priority = priority,
            assigneeName = assigneeName,
            dueAt = dueAt,
            reminderTime = reminderTime,
            description = description,
            meetingId = meetingId,
            deadlineStatus = deadlineStatus(now, dayBounds, upcomingEnd)
        )
    }

    private fun Task.matchesSearch(query: String): Boolean {
        if (query.isBlank()) return true
        return listOf(title, description, assigneeName, category)
            .any { it.contains(query.trim(), ignoreCase = true) }
    }

    private fun Task.matchesFilter(
        filter: TaskDashboardFilter,
        now: Long,
        dayBounds: DayBounds,
        upcomingEnd: Long
    ): Boolean {
        return when (filter) {
            TaskDashboardFilter.ALL -> true
            TaskDashboardFilter.MY_TASKS -> isAssignedToMe()
            TaskDashboardFilter.TODAY -> isDueToday(dayBounds)
            TaskDashboardFilter.OVERDUE -> isOverdue(now)
            TaskDashboardFilter.UPCOMING -> isUpcoming(dayBounds, upcomingEnd)
            TaskDashboardFilter.UNASSIGNED -> isUnassigned()
            TaskDashboardFilter.HIGH_PRIORITY ->
                !isCompleted && priority.equals("High", ignoreCase = true)
            TaskDashboardFilter.FROM_MEETINGS -> meetingId != null
        }
    }

    private fun taskIntelligenceComparator(
        now: Long,
        dayBounds: DayBounds,
        upcomingEnd: Long
    ): Comparator<Task> {
        return compareBy<Task>(
            { deadlineStatusRank(it.deadlineStatus(now, dayBounds, upcomingEnd)) },
            { it.dueAt ?: Long.MAX_VALUE },
            { prioritySortRank(it.priority) },
            { if (it.isCompleted) 1 else 0 },
            { it.title.lowercase(Locale.getDefault()) }
        )
    }

    private fun Task.deadlineStatus(
        now: Long,
        dayBounds: DayBounds,
        upcomingEnd: Long
    ): TaskDeadlineStatus {
        return when {
            isOverdue(now) -> TaskDeadlineStatus.OVERDUE
            isDueToday(dayBounds) -> TaskDeadlineStatus.TODAY
            isUpcoming(dayBounds, upcomingEnd) -> TaskDeadlineStatus.UPCOMING
            else -> TaskDeadlineStatus.NONE
        }
    }

    private fun Task.isOverdue(now: Long): Boolean {
        return !isCompleted && dueAt != null && dueAt < now
    }

    private fun Task.isDueToday(dayBounds: DayBounds): Boolean {
        val deadline = dueAt ?: return false
        return !isCompleted &&
            deadline >= dayBounds.startInclusive &&
            deadline < dayBounds.endExclusive
    }

    private fun Task.isUpcoming(
        dayBounds: DayBounds,
        upcomingEnd: Long
    ): Boolean {
        val deadline = dueAt ?: return false
        return !isCompleted &&
            deadline >= dayBounds.endExclusive &&
            deadline < upcomingEnd
    }

    private fun Task.isAssignedToMe(): Boolean {
        return assigneeName.trim().lowercase(Locale.getDefault()) in MY_ASSIGNEE_NAMES
    }

    private fun Task.isUnassigned(): Boolean {
        return assigneeName.isBlank() ||
            assigneeName.trim().lowercase(Locale.getDefault()) in UNASSIGNED_NAMES
    }

    private fun dayBounds(now: Long): DayBounds {
        val calendar = Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        val start = calendar.timeInMillis
        calendar.add(Calendar.DAY_OF_YEAR, 1)
        return DayBounds(
            startInclusive = start,
            endExclusive = calendar.timeInMillis
        )
    }

    private fun deadlineStatusRank(status: TaskDeadlineStatus): Int {
        return when (status) {
            TaskDeadlineStatus.OVERDUE -> 0
            TaskDeadlineStatus.TODAY -> 1
            TaskDeadlineStatus.UPCOMING -> 2
            TaskDeadlineStatus.NONE -> 3
        }
    }

    private fun priorityRank(priority: String): Int {
        return when {
            priority.equals("High", ignoreCase = true) -> 2
            priority.equals("Medium", ignoreCase = true) -> 1
            else -> 0
        }
    }

    private fun prioritySortRank(priority: String): Int {
        return when {
            priority.equals("High", ignoreCase = true) -> 0
            priority.equals("Medium", ignoreCase = true) -> 1
            else -> 2
        }
    }

    private data class DayBounds(
        val startInclusive: Long,
        val endExclusive: Long
    )

    companion object {
        private const val DASHBOARD_REFRESH_INTERVAL_MILLIS = 60_000L
        private const val UPCOMING_WINDOW_MILLIS = 7L * 24 * 60 * 60 * 1000
        private val MY_ASSIGNEE_NAMES = setOf(
            "me",
            "myself",
            "bản thân",
            "ban than",
            "tôi",
            "toi"
        )
        private val UNASSIGNED_NAMES = setOf(
            "unassigned",
            "unknown",
            "không rõ",
            "khong ro",
            "chưa phân công",
            "chua phan cong"
        )
    }
}
