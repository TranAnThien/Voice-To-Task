package com.project.voicetotask.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.style.TextOverflow
import java.text.DateFormat
import java.util.Date

import androidx.compose.ui.tooling.preview.Preview
import com.project.voicetotask.ui.theme.VoiceToTaskTheme
import com.project.voicetotask.presentation.screens.task.TaskDeadlineStatus

@Composable
fun TaskCard(
    title: String,
    category: String,
    isCompleted: Boolean,
    assigneeName: String = "",
    dueAt: Long? = null,
    reminderTime: Long? = null,
    priority: String = "Medium",
    meetingLinked: Boolean = false,
    deadlineStatus: TaskDeadlineStatus = TaskDeadlineStatus.NONE,
    onCheckedChange: (Boolean) -> Unit,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    priorityColor: Color = MaterialTheme.colorScheme.primary
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(androidx.compose.foundation.layout.IntrinsicSize.Min),
            verticalAlignment = Alignment.Top
        ) {
            // Status bar indicator
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .fillMaxHeight()
                    .background(priorityColor)
            )

            Row(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.Top
            ) {
                Checkbox(
                    checked = isCompleted,
                    onCheckedChange = onCheckedChange,
                    colors = CheckboxDefaults.colors(
                        checkedColor = MaterialTheme.colorScheme.primary,
                        uncheckedColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.width(8.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = if (isCompleted) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.onSurface,
                        textDecoration = if (isCompleted) TextDecoration.LineThrough else TextDecoration.None,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )

                    if (deadlineStatus != TaskDeadlineStatus.NONE && !isCompleted) {
                        Spacer(modifier = Modifier.height(8.dp))
                        TaskStatusChip(deadlineStatus)
                    }
                    if (assigneeName.isNotBlank()) {
                        Spacer(modifier = Modifier.height(6.dp))
                        TaskMetadataLine(
                            icon = Icons.Default.Person,
                            text = assigneeName
                        )
                    }
                    dueAt?.let {
                        TaskMetadataLine(
                            icon = Icons.Default.CalendarMonth,
                            text = "Deadline: ${formatTaskDate(it)}"
                        )
                    }
                    reminderTime?.let {
                        TaskMetadataLine(
                            icon = Icons.Default.Notifications,
                            text = "Reminder: ${formatTaskDate(it)}"
                        )
                    }
                    if (meetingLinked) {
                        TaskMetadataLine(
                            icon = Icons.Default.Groups,
                            text = "From meeting"
                        )
                    }

                    Row(
                        modifier = Modifier.padding(top = 10.dp),
                        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
                    ) {
                        if (category.isNotEmpty()) {
                            TaskChip(text = category)
                        }
                        TaskChip(text = priority)
                    }
                }
            }
        }
    }
}

@Composable
private fun TaskStatusChip(status: TaskDeadlineStatus) {
    val (text, containerColor, contentColor) = when (status) {
        TaskDeadlineStatus.OVERDUE -> Triple(
            "Overdue",
            MaterialTheme.colorScheme.errorContainer,
            MaterialTheme.colorScheme.onErrorContainer
        )
        TaskDeadlineStatus.TODAY -> Triple(
            "Due today",
            MaterialTheme.colorScheme.tertiaryContainer,
            MaterialTheme.colorScheme.onTertiaryContainer
        )
        TaskDeadlineStatus.UPCOMING -> Triple(
            "Upcoming",
            MaterialTheme.colorScheme.primaryContainer,
            MaterialTheme.colorScheme.onPrimaryContainer
        )
        TaskDeadlineStatus.NONE -> return
    }
    AssistChip(
        onClick = {},
        label = { Text(text, style = MaterialTheme.typography.labelSmall) },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = containerColor,
            labelColor = contentColor
        ),
        border = null
    )
}

@Composable
private fun TaskMetadataLine(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String
) {
    Row(
        modifier = Modifier.padding(top = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.width(16.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun TaskChip(text: String) {
    AssistChip(
        onClick = {},
        label = {
            Text(text = text, style = MaterialTheme.typography.labelSmall)
        },
        colors = AssistChipDefaults.assistChipColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.45f)
        ),
        border = null
    )
}

private fun formatTaskDate(millis: Long): String {
    return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT)
        .format(Date(millis))
}

@Preview(showBackground = true)
@Composable
fun TaskCardPreview() {
    VoiceToTaskTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            TaskCard(
                title = "Meeting with Design Team",
                category = "Work",
                isCompleted = false,
                onCheckedChange = {},
                onClick = {}
            )
            Spacer(modifier = Modifier.height(16.dp))
            TaskCard(
                title = "Buy groceries",
                category = "Personal",
                isCompleted = true,
                onCheckedChange = {},
                onClick = {}
            )
        }
    }
}
