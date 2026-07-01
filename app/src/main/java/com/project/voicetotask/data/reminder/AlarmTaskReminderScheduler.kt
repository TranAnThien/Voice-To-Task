package com.project.voicetotask.data.reminder

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.net.Uri
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.reminder.TaskReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class AlarmTaskReminderScheduler @Inject constructor(
    @param:ApplicationContext private val context: Context
) : TaskReminderScheduler {

    private val alarmManager: AlarmManager =
        context.getSystemService(AlarmManager::class.java)

    override fun schedule(task: Task) {
        val reminderTime = task.reminderTime
        if (reminderTime == null || reminderTime <= System.currentTimeMillis() || task.isCompleted) {
            cancel(task.id)
            return
        }

        alarmManager.setAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            reminderTime,
            reminderPendingIntent(task)
        )
    }

    override fun cancel(taskId: String) {
        val pendingIntent = reminderPendingIntent(taskId)
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
    }

    private fun reminderPendingIntent(task: Task): PendingIntent {
        val intent = baseReminderIntent(task.id)
            .putExtra(TaskReminderContract.EXTRA_TASK_TITLE, task.title)
            .putExtra(TaskReminderContract.EXTRA_TASK_DESCRIPTION, task.description)
            .putExtra(TaskReminderContract.EXTRA_TASK_ASSIGNEE, task.assigneeName)
            .putExtra(TaskReminderContract.EXTRA_TASK_PRIORITY, task.priority)

        task.dueAt?.let {
            intent.putExtra(TaskReminderContract.EXTRA_TASK_DUE_AT, it)
        }

        return PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun reminderPendingIntent(taskId: String): PendingIntent {
        return PendingIntent.getBroadcast(
            context,
            0,
            baseReminderIntent(taskId),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    private fun baseReminderIntent(taskId: String): Intent {
        return Intent(context, TaskReminderReceiver::class.java).apply {
            action = TaskReminderContract.ACTION_TASK_REMINDER
            data = Uri.parse("voicetotask://reminder/$taskId")
            putExtra(TaskReminderContract.EXTRA_TASK_ID, taskId)
        }
    }
}
