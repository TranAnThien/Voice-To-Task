package com.project.voicetotask.presentation.screens.task

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.TaskRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.util.UUID
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class TaskDetailViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val taskId: String = savedStateHandle["taskId"] ?: NEW_TASK_ID
    private val isCreateMode = taskId.isBlank() || taskId == NEW_TASK_ID
    private var loadedTask: Task? = null

    private val _uiState = MutableStateFlow(
        TaskDetailUiState(
            taskId = if (isCreateMode) "" else taskId,
            isEditMode = !isCreateMode
        )
    )
    val uiState: StateFlow<TaskDetailUiState> = _uiState.asStateFlow()

    init {
        if (!isCreateMode) {
            loadTask(taskId)
        } else {
            updateCanSave()
        }
    }

    fun onTitleChange(title: String) {
        _uiState.update { it.copy(title = title, errorMessage = null) }
        updateCanSave()
    }

    fun onNotesChange(notes: String) {
        _uiState.update { it.copy(notes = notes) }
    }

    fun onCategoryChange(category: String) {
        _uiState.update { it.copy(category = category) }
    }

    fun onPriorityChange(priority: String) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onAssigneeChange(assigneeName: String) {
        _uiState.update { it.copy(assigneeName = assigneeName, errorMessage = null) }
    }

    fun onDueAtChange(dueAt: Long?) {
        _uiState.update { it.copy(dueAt = dueAt, errorMessage = null) }
    }

    fun onCompletedChange(isCompleted: Boolean) {
        _uiState.update { it.copy(isCompleted = isCompleted) }
    }

    fun onReminderChange(reminderTime: Long?) {
        _uiState.update { it.copy(reminderTime = reminderTime, errorMessage = null) }
    }

    fun onNotificationPermissionDenied() {
        _uiState.update {
            it.copy(errorMessage = "Notification permission is required for task reminders.")
        }
    }

    fun save() {
        val state = _uiState.value
        val title = state.title.trim()
        if (title.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Task title is required", canSave = false) }
            return
        }
        if (state.reminderTime != null && state.reminderTime <= System.currentTimeMillis()) {
            _uiState.update {
                it.copy(errorMessage = "Reminder time must be in the future.")
            }
            return
        }
        if (state.dueAt != null && state.dueAt <= System.currentTimeMillis()) {
            _uiState.update {
                it.copy(errorMessage = "Deadline must be in the future.")
            }
            return
        }
        if (
            state.reminderTime != null &&
            state.dueAt != null &&
            state.reminderTime > state.dueAt
        ) {
            _uiState.update {
                it.copy(errorMessage = "Reminder should be before the deadline.")
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val task = if (state.isEditMode) {
                val existing = loadedTask ?: taskRepository.getTaskOnce(taskId)
                existing?.copy(
                    title = title,
                    description = state.notes.trim(),
                    category = state.category,
                    priority = state.priority,
                    assigneeName = state.assigneeName.trim().ifBlank { "Me" },
                    dueAt = state.dueAt,
                    isCompleted = state.isCompleted,
                    reminderTime = state.reminderTime
                )
            } else {
                Task(
                    id = UUID.randomUUID().toString(),
                    title = title,
                    description = state.notes.trim(),
                    category = state.category,
                    priority = state.priority,
                    isCompleted = state.isCompleted,
                    reminderTime = state.reminderTime,
                    meetingId = null,
                    assigneeName = state.assigneeName.trim().ifBlank { "Me" },
                    dueAt = state.dueAt
                )
            }

            if (task == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Task not found"
                    )
                }
                return@launch
            }

            if (state.isEditMode) {
                taskRepository.updateTask(task)
            } else {
                taskRepository.insertTask(task)
            }
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    fun delete() {
        if (isCreateMode) {
            _uiState.update { it.copy(isFinished = true) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            taskRepository.deleteTaskById(taskId)
            _uiState.update { it.copy(isLoading = false, isFinished = true) }
        }
    }

    private fun loadTask(id: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            val task = taskRepository.getTaskOnce(id)
            loadedTask = task
            if (task == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = "Task not found"
                    )
                }
                return@launch
            }

            _uiState.update {
                it.copy(
                    taskId = task.id,
                    title = task.title,
                    notes = task.description,
                    category = task.category,
                    priority = task.priority,
                    assigneeName = task.assigneeName,
                    dueAt = task.dueAt,
                    isCompleted = task.isCompleted,
                    reminderTime = task.reminderTime,
                    isLoading = false,
                    isEditMode = true,
                    canSave = task.title.isNotBlank()
                )
            }
        }
    }

    private fun updateCanSave() {
        _uiState.update { it.copy(canSave = it.title.isNotBlank()) }
    }

    companion object {
        const val NEW_TASK_ID = "new"
    }
}
