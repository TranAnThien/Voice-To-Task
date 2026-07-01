package com.project.voicetotask.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.model.Meeting
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import com.project.voicetotask.presentation.export.ExportFormatter
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.Calendar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow(HistoryFilter.ALL)
    private var latestMeetings: List<Meeting> = emptyList()
    private var latestTasks: List<Task> = emptyList()

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = combine(
        meetingRepository.getAllMeetings(),
        taskRepository.getAllTasks(),
        _searchQuery,
        _activeFilter
    ) { meetings, tasks, query, filter ->
        latestMeetings = meetings
        latestTasks = tasks
        val now = System.currentTimeMillis()
        val tasksByMeetingId = tasks
            .filter { it.meetingId != null }
            .groupBy { it.meetingId.orEmpty() }

        val filteredMeetings = meetings
            .asSequence()
            .filter { meeting -> meeting.matchesQuery(query, tasksByMeetingId[meeting.id].orEmpty()) }
            .filter { meeting -> meeting.matchesFilter(filter, tasksByMeetingId[meeting.id].orEmpty(), now) }
            .sortedByDescending { it.date }
            .toList()

        val meetingModels = filteredMeetings.map { meeting ->
            val linkedTasks = tasksByMeetingId[meeting.id].orEmpty()
            MeetingModel(
                id = meeting.id,
                title = meeting.title,
                date = dateFormatter.format(Date(meeting.date)),
                duration = formatDuration(meeting.duration),
                taskCount = linkedTasks.size,
                summaryPreview = meeting.summary
                    .ifBlank { meeting.transcript }
                    .replace(Regex("\\s+"), " ")
                    .trim()
                    .take(140),
                badges = meeting.createBadges(linkedTasks, now),
                assigneePreview = linkedTasks.createAssigneePreview()
            )
        }

        HistoryUiState(
            meetings = meetingModels,
            searchQuery = query,
            activeFilter = filter,
            emptyMessage = if (query.isNotBlank()) {
                "No meetings match \"$query\""
            } else {
                filter.emptyMessage
            }
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: HistoryFilter) {
        _activeFilter.value = filter
    }

    fun buildBackupJson(): String {
        return ExportFormatter.backupJson(
            meetings = latestMeetings,
            tasks = latestTasks
        )
    }

    private fun formatDuration(millis: Long): String {
        if (millis <= 0L) return "--:--"
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun Meeting.matchesQuery(query: String, linkedTasks: List<Task>): Boolean {
        val normalizedQuery = query.trim().lowercase(Locale.getDefault())
        if (normalizedQuery.isBlank()) return true

        return searchableText(linkedTasks)
            .lowercase(Locale.getDefault())
            .contains(normalizedQuery)
    }

    private fun Meeting.searchableText(linkedTasks: List<Task>): String {
        return buildString {
            append(title).append(' ')
            append(transcript).append(' ')
            append(summary).append(' ')
            append(decisionsText).append(' ')
            append(blockersText).append(' ')
            append(followUpsText).append(' ')
            linkedTasks.forEach { task ->
                append(task.title).append(' ')
                append(task.description).append(' ')
                append(task.assigneeName).append(' ')
                append(task.category).append(' ')
            }
        }
    }

    private fun Meeting.matchesFilter(
        filter: HistoryFilter,
        linkedTasks: List<Task>,
        now: Long
    ): Boolean {
        return when (filter) {
            HistoryFilter.ALL -> true
            HistoryFilter.THIS_WEEK -> date >= startOfWeek(now)
            HistoryFilter.THIS_MONTH -> date >= startOfMonth(now)
            HistoryFilter.HAS_TASKS -> linkedTasks.isNotEmpty()
            HistoryFilter.NO_TASKS -> linkedTasks.isEmpty()
            HistoryFilter.HAS_BLOCKERS -> blockersText.isNotBlank()
            HistoryFilter.HAS_DECISIONS -> decisionsText.isNotBlank()
            HistoryFilter.HAS_FOLLOW_UPS -> followUpsText.isNotBlank()
            HistoryFilter.HAS_OVERDUE_TASKS -> linkedTasks.any { it.isOverdue(now) }
            HistoryFilter.ASSIGNED_TO_ME -> linkedTasks.any { it.isAssignedToMe() }
        }
    }

    private fun Meeting.createBadges(linkedTasks: List<Task>, now: Long): List<String> {
        return buildList {
            if (decisionsText.isNotBlank()) add("Decisions")
            if (blockersText.isNotBlank()) add("Blockers")
            if (followUpsText.isNotBlank()) add("Follow-ups")
            if (linkedTasks.any { it.isOverdue(now) }) add("Overdue tasks")
        }
    }

    private fun List<Task>.createAssigneePreview(): String {
        val assignees = map { it.assigneeName.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.getDefault()) }
        return when {
            assignees.isEmpty() -> ""
            assignees.size <= 2 -> assignees.joinToString(", ")
            else -> "${assignees.take(2).joinToString(", ")} +${assignees.size - 2}"
        }
    }

    private fun Task.isOverdue(now: Long): Boolean {
        return !isCompleted && dueAt != null && dueAt < now
    }

    private fun Task.isAssignedToMe(): Boolean {
        return assigneeName.trim().lowercase(Locale.getDefault()) in MY_ASSIGNEE_NAMES
    }

    private fun startOfWeek(now: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now
            firstDayOfWeek = Calendar.MONDAY
            set(Calendar.DAY_OF_WEEK, firstDayOfWeek)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    private fun startOfMonth(now: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = now
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    companion object {
        private val MY_ASSIGNEE_NAMES = setOf(
            "me",
            "myself",
            "b\u1ea3n th\u00e2n",
            "ban than",
            "t\u00f4i",
            "toi"
        )
    }
}
