package com.project.voicetotask.data.repository

import com.project.voicetotask.data.mapper.toDomain
import com.project.voicetotask.data.mapper.toEntity
import com.project.voicetotask.data.source.local.dao.TaskDao
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.reminder.TaskReminderScheduler
import com.project.voicetotask.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject

class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val reminderScheduler: TaskReminderScheduler
) : TaskRepository {

    override fun getAllTasks(): Flow<List<Task>> {
        return taskDao.getAllTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTasksForMeeting(meetingId: String): Flow<List<Task>> {
        return taskDao.getTasksForMeeting(meetingId).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getTaskById(id: String): Flow<Task?> {
        return taskDao.getTaskById(id).map { it?.toDomain() }
    }

    override suspend fun getTaskOnce(id: String): Task? {
        return taskDao.getTaskOnce(id)?.toDomain()
    }

    override suspend fun insertTask(task: Task) {
        taskDao.insertTask(task.toEntity())
        syncReminder(task)
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
        syncReminder(task)
    }

    override suspend fun deleteTask(task: Task) {
        reminderScheduler.cancel(task.id)
        taskDao.deleteTask(task.toEntity())
    }

    override suspend fun deleteTaskById(id: String) {
        reminderScheduler.cancel(id)
        taskDao.deleteTaskById(id)
    }

    override suspend fun setTaskCompleted(id: String, isCompleted: Boolean) {
        val task = taskDao.getTaskOnce(id)?.toDomain()
        taskDao.setTaskCompleted(id, isCompleted)
        task?.let { syncReminder(it.copy(isCompleted = isCompleted)) }
    }

    private fun syncReminder(task: Task) {
        if (
            task.reminderTime != null &&
            task.reminderTime > System.currentTimeMillis() &&
            !task.isCompleted
        ) {
            reminderScheduler.schedule(task)
        } else {
            reminderScheduler.cancel(task.id)
        }
    }
}
