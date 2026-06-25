package com.project.voicetotask.presentation.screens.record

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import com.project.voicetotask.presentation.screens.task.TaskModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AiResultViewModel @Inject constructor(
    private val taskRepository: TaskRepository,
    private val meetingRepository: MeetingRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val meetingId: String? = savedStateHandle["meetingId"]

    private val _uiState = MutableStateFlow(AiResultUiState())
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
                    _uiState.update { it.copy(summary = m.transcript) }
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
                            isCompleted = task.isCompleted
                        )
                    })
                }
            }
        }
    }

    fun onTaskDismissed(task: TaskModel) {
        viewModelScope.launch {
            taskRepository.deleteTaskById(task.id)
        }
    }

    fun onTaskCheckedChange(taskId: String, isCompleted: Boolean) {
        viewModelScope.launch {
            taskRepository.setTaskCompleted(taskId, isCompleted)
        }
    }

    fun saveChanges() {
        // Most changes are already saved reactively in the local Room flow.
        // In a more complex flow, we'd batch updates here.
    }
}
