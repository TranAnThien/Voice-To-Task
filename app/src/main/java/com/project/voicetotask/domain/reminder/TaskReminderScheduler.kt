package com.project.voicetotask.domain.reminder

import com.project.voicetotask.domain.model.Task

interface TaskReminderScheduler {
    fun schedule(task: Task)
    fun cancel(taskId: String)
}
