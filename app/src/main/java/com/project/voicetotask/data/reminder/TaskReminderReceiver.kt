package com.project.voicetotask.data.reminder

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.project.voicetotask.MainActivity
import com.project.voicetotask.R
import java.text.DateFormat
import java.util.Date

class TaskReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getStringExtra(TaskReminderContract.EXTRA_TASK_ID) ?: return
        val title = intent.getStringExtra(TaskReminderContract.EXTRA_TASK_TITLE)
            ?.takeIf { it.isNotBlank() }
            ?: "Task reminder"
        val description = intent.getStringExtra(TaskReminderContract.EXTRA_TASK_DESCRIPTION)
            ?.takeIf { it.isNotBlank() }
        val assignee = intent.getStringExtra(TaskReminderContract.EXTRA_TASK_ASSIGNEE)
            ?.takeIf { it.isNotBlank() }
        val priority = intent.getStringExtra(TaskReminderContract.EXTRA_TASK_PRIORITY)
            ?.takeIf { it.isNotBlank() }
        val dueAt = intent.getLongExtra(TaskReminderContract.EXTRA_TASK_DUE_AT, 0L)
            .takeIf { it > 0L }
        val notificationBody = buildNotificationBody(
            description = description,
            assignee = assignee,
            priority = priority,
            dueAt = dueAt
        )

        if (
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) !=
            PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        createNotificationChannel(context)

        val openTaskIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                Intent.FLAG_ACTIVITY_CLEAR_TOP or
                Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra(TaskReminderContract.EXTRA_TASK_ID, taskId)
        }
        val contentIntent = PendingIntent.getActivity(
            context,
            taskId.hashCode(),
            openTaskIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, TaskReminderContract.CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(notificationBody)
            .setStyle(NotificationCompat.BigTextStyle().bigText(notificationBody))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(contentIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(taskId.hashCode(), notification)
    }

    private fun createNotificationChannel(context: Context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return

        val channel = NotificationChannel(
            TaskReminderContract.CHANNEL_ID,
            TaskReminderContract.CHANNEL_NAME,
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications for scheduled task reminders"
        }

        context.getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
    }

    private fun buildNotificationBody(
        description: String?,
        assignee: String?,
        priority: String?,
        dueAt: Long?
    ): String {
        val lines = mutableListOf("Reminder to start working on this task.")
        assignee?.let { lines += "Assignee: $it" }
        dueAt?.let {
            val formattedDeadline = DateFormat.getDateTimeInstance(
                DateFormat.MEDIUM,
                DateFormat.SHORT
            ).format(Date(it))
            lines += "Deadline: $formattedDeadline"
        }
        priority?.let { lines += "Priority: $it" }
        description?.let { lines += it }
        return lines.joinToString(separator = "\n")
    }
}
