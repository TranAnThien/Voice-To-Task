package com.project.voicetotask.presentation.screens.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class HistoryViewModel @Inject constructor(
    private val meetingRepository: MeetingRepository,
    private val taskRepository: TaskRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeFilter = MutableStateFlow("All")

    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())

    val uiState: StateFlow<HistoryUiState> = combine(
        _searchQuery.flatMapLatest { query ->
            if (query.isEmpty()) meetingRepository.getAllMeetings()
            else meetingRepository.searchMeetings(query)
        },
        taskRepository.getAllTasks(),
        _activeFilter
    ) { meetings, tasks, filter ->
        val taskCountsByMeetingId = tasks
            .mapNotNull { task -> task.meetingId }
            .groupingBy { meetingId -> meetingId }
            .eachCount()

        val filteredMeetings = meetings.filter { meeting ->
            when (filter) {
                "All" -> true
                "This week" -> {
                    val oneWeekAgo = System.currentTimeMillis() - (7 * 24 * 60 * 60 * 1000)
                    meeting.date >= oneWeekAgo
                }
                "This month" -> {
                    val oneMonthAgo = System.currentTimeMillis() - (30L * 24 * 60 * 60 * 1000)
                    meeting.date >= oneMonthAgo
                }
                "Created task" -> taskCountsByMeetingId[meeting.id].orZero() > 0
                else -> true
            }
        }

        val meetingModels = filteredMeetings.map { meeting ->
            MeetingModel(
                id = meeting.id,
                title = meeting.title,
                date = dateFormatter.format(Date(meeting.date)),
                duration = formatDuration(meeting.duration),
                taskCount = taskCountsByMeetingId[meeting.id].orZero()
            )
        }

        HistoryUiState(
            meetings = meetingModels,
            searchQuery = _searchQuery.value,
            activeFilter = filter
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HistoryUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onFilterChange(filter: String) {
        _activeFilter.value = filter
    }

    private fun formatDuration(millis: Long): String {
        val seconds = (millis / 1000) % 60
        val minutes = (millis / (1000 * 60)) % 60
        val hours = (millis / (1000 * 60 * 60))
        return if (hours > 0) {
            String.format("%02d:%02d:%02d", hours, minutes, seconds)
        } else {
            String.format("%02d:%02d", minutes, seconds)
        }
    }

    private fun Int?.orZero(): Int = this ?: 0
}
