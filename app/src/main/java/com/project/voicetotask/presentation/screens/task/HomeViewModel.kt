package com.project.voicetotask.presentation.screens.task

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val meetingRepository: MeetingRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    private val _activeCategory = MutableStateFlow("All")

    val uiState: StateFlow<HomeUiState> = combine(
        taskRepository.getAllTasks(),
        meetingRepository.getAllMeetings(),
        _searchQuery,
        _activeCategory
    ) { tasks, meetings, query, category ->
        val filteredTasks = tasks.filter { task ->
            (category == "All" || task.category == category) &&
            (query.isEmpty() || task.title.contains(query, ignoreCase = true))
        }.map { task ->
            TaskModel(
                id = task.id,
                title = task.title,
                category = task.category ?: "Other",
                isCompleted = task.isCompleted,
                priorityCode = when (task.priority) {
                    "High" -> 2
                    "Medium" -> 1
                    else -> 0
                }
            )
        }

        HomeUiState(
            userName = "User", // Mock for now
            tasksToDoTodayCount = tasks.count { !it.isCompleted },
            recordedMeetingsCount = meetings.size,
            searchQuery = query,
            activeCategory = category,
            recentTasks = filteredTasks
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = HomeUiState()
    )

    fun onSearchQueryChange(query: String) {
        _searchQuery.value = query
    }

    fun onCategorySelected(category: String) {
        _activeCategory.value = category
    }

    fun toggleTaskCompletion(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, isCompleted)
        }
    }
}
