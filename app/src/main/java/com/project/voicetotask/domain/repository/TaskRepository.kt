package com.project.voicetotask.domain.repository

import com.project.voicetotask.domain.model.Task
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun getAllTasks(): Flow<List<Task>>
    fun getTasksForMeeting(meetingId: String): Flow<List<Task>>
    fun getTaskById(id: String): Flow<Task?>
    suspend fun getTaskOnce(id: String): Task?
    suspend fun insertTask(task: Task)
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(task: Task)
    suspend fun deleteTaskById(id: String)
    suspend fun setTaskCompleted(id: String, isCompleted: Boolean)
}
