package com.project.voicetotask.presentation.screens.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.model.DEFAULT_ASSIGNEE
import com.project.voicetotask.domain.model.Meeting
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import com.project.voicetotask.presentation.screens.task.TaskModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.io.File
import java.util.UUID
import javax.inject.Inject

@HiltViewModel
class AiResultViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val meetingRepository: MeetingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val meetingId: String? = savedStateHandle["meetingId"]
    private var audioFilePath: String? = null
    private var currentMeeting: Meeting? = null
    private var hasEditedMeeting = false

    private val _uiState = MutableStateFlow(AiResultUiState(isLoading = meetingId != null))
    val uiState: StateFlow<AiResultUiState> = _uiState.asStateFlow()

    init {
        meetingId?.let { id ->
            loadMeetingData(id)
        }
    }

    private fun loadMeetingData(id: String) {
        viewModelScope.launch {
            meetingRepository.getMeetingById(id).collect { meeting ->
                meeting?.let { m ->
                    currentMeeting = m
                    audioFilePath = m.audioFilePath
                    _uiState.update { state ->
                        if (hasEditedMeeting) {
                            state.copy(isLoading = false)
                        } else {
                            state.copy(
                                transcript = m.transcript,
                                meetingSummary = m.summary,
                                decisionsText = m.decisionsText,
                                blockersText = m.blockersText,
                                followUpsText = m.followUpsText,
                                isLoading = false
                            )
                        }
                    }
                }
            }
        }
        viewModelScope.launch {
            taskRepository.getTasksForMeeting(id).collect { tasks ->
                _uiState.update { state ->
                    state.copy(tasks = tasks.map { task ->
                        TaskModel(
                            id = task.id,
                            title = task.title,
                            category = task.category,
                            isCompleted = task.isCompleted,
                            priority = task.priority,
                            priorityCode = when (task.priority) {
                                "High" -> 2
                                "Medium" -> 1
                                else -> 0
                            },
                            assigneeName = task.assigneeName,
                            dueAt = task.dueAt,
                            reminderTime = task.reminderTime,
                            description = task.description,
                            meetingId = task.meetingId
                        )
                    })
                }
            }
        }
    }

    fun onMeetingSummaryChange(summary: String) {
        hasEditedMeeting = true
        _uiState.update { it.copy(meetingSummary = summary, errorMessage = null) }
    }

    fun onDecisionsChange(decisionsText: String) {
        hasEditedMeeting = true
        _uiState.update { it.copy(decisionsText = decisionsText, errorMessage = null) }
    }

    fun onBlockersChange(blockersText: String) {
        hasEditedMeeting = true
        _uiState.update { it.copy(blockersText = blockersText, errorMessage = null) }
    }

    fun onFollowUpsChange(followUpsText: String) {
        hasEditedMeeting = true
        _uiState.update { it.copy(followUpsText = followUpsText, errorMessage = null) }
    }

    fun startAddingTask() {
        _uiState.update {
            it.copy(
                editingTask = AiReviewTaskDraft(),
                errorMessage = null
            )
        }
    }

    fun startEditingTask(task: TaskModel) {
        _uiState.update {
            it.copy(
                editingTask = AiReviewTaskDraft(
                    id = task.id,
                    title = task.title,
                    description = task.description,
                    assigneeName = task.assigneeName.ifBlank { DEFAULT_ASSIGNEE },
                    dueAt = task.dueAt,
                    category = task.category.ifBlank { "Other" },
                    priority = task.priority.ifBlank { "Medium" },
                    isCompleted = task.isCompleted,
                    reminderTime = task.reminderTime
                ),
                errorMessage = null
            )
        }
    }

    fun cancelTaskEditing() {
        _uiState.update { it.copy(editingTask = null) }
    }

    fun onEditingTaskTitleChange(title: String) {
        updateEditingTask { it.copy(title = title) }
    }

    fun onEditingTaskDescriptionChange(description: String) {
        updateEditingTask { it.copy(description = description) }
    }

    fun onEditingTaskAssigneeChange(assigneeName: String) {
        updateEditingTask { it.copy(assigneeName = assigneeName) }
    }

    fun onEditingTaskDueAtChange(dueAt: Long?) {
        updateEditingTask { it.copy(dueAt = dueAt) }
    }

    fun onEditingTaskCategoryChange(category: String) {
        updateEditingTask { it.copy(category = category) }
    }

    fun onEditingTaskPriorityChange(priority: String) {
        updateEditingTask { it.copy(priority = priority) }
    }

    fun saveEditingTask() {
        val draft = _uiState.value.editingTask ?: return
        val id = meetingId ?: return
        val title = draft.title.trim()
        if (title.isEmpty()) {
            _uiState.update { it.copy(errorMessage = "Task title is required.") }
            return
        }

        viewModelScope.launch {
            try {
                val task = if (draft.isNew) {
                    Task(
                        id = UUID.randomUUID().toString(),
                        title = title,
                        description = draft.description.trim(),
                        category = draft.category.trim().ifBlank { "Other" },
                        priority = draft.priority.trim().ifBlank { "Medium" },
                        isCompleted = draft.isCompleted,
                        reminderTime = draft.reminderTime,
                        meetingId = id,
                        assigneeName = draft.assigneeName.trim().ifBlank { DEFAULT_ASSIGNEE },
                        dueAt = draft.dueAt
                    )
                } else {
                    val existing = taskRepository.getTaskOnce(draft.id.orEmpty()) ?: return@launch
                    existing.copy(
                        title = title,
                        description = draft.description.trim(),
                        assigneeName = draft.assigneeName.trim().ifBlank { DEFAULT_ASSIGNEE },
                        dueAt = draft.dueAt,
                        category = draft.category.trim().ifBlank { "Other" },
                        priority = draft.priority.trim().ifBlank { "Medium" }
                    )
                }
                if (draft.isNew) {
                    taskRepository.insertTask(task)
                } else {
                    taskRepository.updateTask(task)
                }
                _uiState.update { it.copy(editingTask = null, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update { it.copy(errorMessage = "Could not save the task draft.") }
            }
        }
    }

    fun requestTaskDelete(task: TaskModel) {
        _uiState.update { it.copy(taskPendingDelete = task, errorMessage = null) }
    }

    fun cancelTaskDelete() {
        _uiState.update { it.copy(taskPendingDelete = null) }
    }

    fun confirmTaskDelete() {
        val task = _uiState.value.taskPendingDelete ?: return
        viewModelScope.launch {
            try {
                taskRepository.deleteTaskById(task.id)
                _uiState.update { it.copy(taskPendingDelete = null, errorMessage = null) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        taskPendingDelete = null,
                        errorMessage = "Could not delete the generated task."
                    )
                }
            }
        }
    }

    fun onTaskCheckedChange(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, isCompleted)
        }
    }

    fun saveChanges() {
        val id = meetingId ?: return
        if (_uiState.value.isSaving || _uiState.value.isFinished) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, showExitDialog = false, errorMessage = null)
            }
            try {
                saveMeetingReview(id)
                meetingRepository.confirmMeeting(id)
                _uiState.update { it.copy(isSaving = false, isFinished = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Could not save the analysis result."
                    )
                }
            }
        }
    }

    fun requestBack() {
        if (!_uiState.value.isSaving) {
            _uiState.update { it.copy(showExitDialog = true) }
        }
    }

    fun stayOnResult() {
        _uiState.update { it.copy(showExitDialog = false) }
    }

    fun discard() {
        val id = meetingId ?: return
        if (_uiState.value.isSaving || _uiState.value.isFinished) return

        viewModelScope.launch {
            _uiState.update {
                it.copy(isSaving = true, showExitDialog = false, errorMessage = null)
            }
            try {
                meetingRepository.deleteMeetingById(id)
                audioFilePath?.let { path ->
                    File(path).takeIf { it.exists() }?.delete()
                }
                _uiState.update { it.copy(isSaving = false, isFinished = true) }
            } catch (e: Exception) {
                _uiState.update {
                    it.copy(
                        isSaving = false,
                        errorMessage = "Could not discard the analysis result."
                    )
                }
            }
        }
    }

    private fun updateEditingTask(update: (AiReviewTaskDraft) -> AiReviewTaskDraft) {
        _uiState.update { state ->
            val draft = state.editingTask ?: return@update state
            state.copy(editingTask = update(draft), errorMessage = null)
        }
    }

    private suspend fun saveMeetingReview(id: String) {
        val meeting = currentMeeting ?: meetingRepository.getMeetingOnce(id) ?: return
        val state = _uiState.value
        meetingRepository.updateMeeting(
            meeting.copy(
                summary = state.meetingSummary.trim(),
                decisionsText = normalizeMultilineText(state.decisionsText),
                blockersText = normalizeMultilineText(state.blockersText),
                followUpsText = normalizeMultilineText(state.followUpsText)
            )
        )
        currentMeeting = meeting.copy(
            summary = state.meetingSummary.trim(),
            decisionsText = normalizeMultilineText(state.decisionsText),
            blockersText = normalizeMultilineText(state.blockersText),
            followUpsText = normalizeMultilineText(state.followUpsText)
        )
    }

    private fun normalizeMultilineText(value: String): String {
        return value.lineSequence()
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .joinToString("\n")
    }
}
