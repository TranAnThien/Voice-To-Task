package com.project.voicetotask.presentation.screens.record

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.PrimaryButton
import com.project.voicetotask.presentation.components.TaskCard
import com.project.voicetotask.presentation.screens.task.TaskModel

import androidx.compose.ui.tooling.preview.Preview
import com.project.voicetotask.ui.theme.VoiceToTaskTheme
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiResultScreen(
    uiState: AiResultUiState,
    onBackClick: () -> Unit,
    onSaveTasksClick: () -> Unit,
    onDiscardClick: () -> Unit,
    onStayClick: () -> Unit,
    onMeetingSummaryChange: (String) -> Unit,
    onDecisionsChange: (String) -> Unit,
    onBlockersChange: (String) -> Unit,
    onFollowUpsChange: (String) -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskDismiss: (TaskModel) -> Unit,
    onTaskClick: (TaskModel) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    onCancelTaskDelete: () -> Unit,
    onConfirmTaskDelete: () -> Unit,
    onTaskTitleChange: (String) -> Unit,
    onTaskDescriptionChange: (String) -> Unit,
    onTaskAssigneeChange: (String) -> Unit,
    onTaskDueAtChange: (Long?) -> Unit,
    onTaskCategoryChange: (String) -> Unit,
    onTaskPriorityChange: (String) -> Unit,
    onSaveTaskEdit: () -> Unit,
    onCancelTaskEdit: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(enabled = !uiState.isFinished) {
        onBackClick()
    }

    if (uiState.showExitDialog) {
        AlertDialog(
            onDismissRequest = onStayClick,
            title = { Text("Chưa lưu kết quả") },
            text = { Text("Bạn có muốn lưu kết quả phân tích này không?") },
            confirmButton = {
                TextButton(
                    onClick = onSaveTasksClick,
                    enabled = !uiState.isSaving
                ) {
                    Text("Lưu lại")
                }
            },
            dismissButton = {
                Row {
                    TextButton(
                        onClick = onDiscardClick,
                        enabled = !uiState.isSaving
                    ) {
                        Text("Không lưu")
                    }
                    TextButton(onClick = onStayClick) {
                        Text("Ở lại")
                    }
                }
            }
        )
    }

    uiState.taskPendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = onCancelTaskDelete,
            title = { Text("Delete generated task?") },
            text = { Text("This removes \"${task.title}\" from the draft result. It will not be saved.") },
            confirmButton = {
                TextButton(
                    onClick = onConfirmTaskDelete,
                    enabled = !uiState.isSaving
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = onCancelTaskDelete) {
                    Text("Cancel")
                }
            }
        )
    }

    uiState.editingTask?.let { draft ->
        TaskReviewEditorDialog(
            draft = draft,
            onTitleChange = onTaskTitleChange,
            onDescriptionChange = onTaskDescriptionChange,
            onAssigneeChange = onTaskAssigneeChange,
            onDueAtChange = onTaskDueAtChange,
            onCategoryChange = onTaskCategoryChange,
            onPriorityChange = onTaskPriorityChange,
            onSave = onSaveTaskEdit,
            onCancel = onCancelTaskEdit
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tasks_from_meeting)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDiscardClick,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Discard")
                }
                PrimaryButton(
                    text = if (uiState.isSaving) "Saving..." else "Save result",
                    onClick = onSaveTasksClick,
                    enabled = !uiState.isSaving,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp),
            contentPadding = PaddingValues(bottom = 24.dp)
        ) {
            item {
                if (uiState.isLoading) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }
                EditableMeetingIntelligenceSection(
                    title = stringResource(id = R.string.meeting_summary),
                    content = uiState.meetingSummary,
                    placeholder = "AI did not create a summary yet.",
                    onContentChange = onMeetingSummaryChange
                )
                EditableMeetingIntelligenceSection(
                    title = stringResource(id = R.string.key_decisions),
                    content = uiState.decisionsText,
                    placeholder = "One decision per line.",
                    onContentChange = onDecisionsChange
                )
                EditableMeetingIntelligenceSection(
                    title = stringResource(id = R.string.blockers_risks),
                    content = uiState.blockersText,
                    placeholder = "One blocker, risk, or dependency per line.",
                    onContentChange = onBlockersChange
                )
                EditableMeetingIntelligenceSection(
                    title = stringResource(id = R.string.follow_ups),
                    content = uiState.followUpsText,
                    placeholder = "One follow-up question or action per line.",
                    onContentChange = onFollowUpsChange
                )
                Text(
                    text = stringResource(id = R.string.transcript),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceContainer
                    )
                ) {
                    Text(
                        text = uiState.transcript.ifBlank { "Transcript is unavailable." },
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(18.dp)
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(id = R.string.task_list),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedButton(onClick = onAddTaskClick, enabled = !uiState.isSaving) {
                    Text("Add task")
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.tasks.isEmpty()) {
                    Text(
                        text = "AI did not find any tasks. You can add one manually before saving.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
                uiState.errorMessage?.let { message ->
                    Text(
                        text = message,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
            
            items(uiState.tasks, key = { it.id }) { task ->
                val dismissState = rememberSwipeToDismissBoxState(
                    confirmValueChange = { dismissValue ->
                        if (dismissValue == SwipeToDismissBoxValue.EndToStart) {
                            onTaskDismiss(task)
                            true
                        } else {
                            false
                        }
                    }
                )
                
                SwipeToDismissBox(
                    state = dismissState,
                    backgroundContent = {
                        val color = if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                            MaterialTheme.colorScheme.errorContainer
                        } else {
                            Color.Transparent
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(vertical = 6.dp)
                                .clip(MaterialTheme.shapes.medium)
                                .background(color),
                            contentAlignment = Alignment.CenterEnd
                        ) {
                            if (dismissState.dismissDirection == SwipeToDismissBoxValue.EndToStart) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete",
                                    modifier = Modifier.padding(end = 16.dp),
                                    tint = MaterialTheme.colorScheme.onErrorContainer
                                )
                            }
                        }
                    },
                    enableDismissFromStartToEnd = false
                ) {
                    TaskCard(
                        title = task.title,
                        category = task.category,
                        isCompleted = task.isCompleted,
                        assigneeName = task.assigneeName,
                        dueAt = task.dueAt,
                        reminderTime = task.reminderTime,
                        priority = task.priority,
                        onCheckedChange = { onTaskCheckedChange(task.id, it) },
                        onClick = { onTaskClick(task) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun MeetingIntelligenceSection(
    title: String,
    content: String
) {
    if (content.isBlank()) return

    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            content.lineSequence()
                .map { it.trim() }
                .filter { it.isNotBlank() }
                .forEach { line ->
                    Text(
                        text = if (content.contains('\n')) "• $line" else line,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                }
        }
    }
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun EditableMeetingIntelligenceSection(
    title: String,
    content: String,
    placeholder: String,
    onContentChange: (String) -> Unit
) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.primary
    )
    Spacer(modifier = Modifier.height(8.dp))
    OutlinedTextField(
        value = content,
        onValueChange = onContentChange,
        placeholder = { Text(placeholder) },
        minLines = if (title == stringResource(id = R.string.meeting_summary)) 3 else 2,
        maxLines = 6,
        modifier = Modifier.fillMaxWidth(),
        textStyle = MaterialTheme.typography.bodyLarge
    )
    Spacer(modifier = Modifier.height(20.dp))
}

@Composable
private fun TaskReviewEditorDialog(
    draft: AiReviewTaskDraft,
    onTitleChange: (String) -> Unit,
    onDescriptionChange: (String) -> Unit,
    onAssigneeChange: (String) -> Unit,
    onDueAtChange: (Long?) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onSave: () -> Unit,
    onCancel: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onCancel,
        title = { Text(if (draft.isNew) "Add task" else "Edit generated task") },
        text = {
            Column {
                OutlinedTextField(
                    value = draft.title,
                    onValueChange = onTitleChange,
                    label = { Text("Title") },
                    isError = draft.title.isBlank(),
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.description,
                    onValueChange = onDescriptionChange,
                    label = { Text("Description") },
                    minLines = 2,
                    maxLines = 4,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(10.dp))
                OutlinedTextField(
                    value = draft.assigneeName,
                    onValueChange = onAssigneeChange,
                    label = { Text("Assignee") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(14.dp))
                TaskReviewChoiceRow(
                    label = "Category",
                    values = listOf("Work", "Meeting", "Personal", "Study", "Other"),
                    selected = draft.category,
                    onSelected = onCategoryChange
                )
                Spacer(modifier = Modifier.height(12.dp))
                TaskReviewChoiceRow(
                    label = "Priority",
                    values = listOf("High", "Medium", "Low"),
                    selected = draft.priority,
                    onSelected = onPriorityChange
                )
                Spacer(modifier = Modifier.height(14.dp))
                TaskReviewDeadlineSelector(
                    dueAt = draft.dueAt,
                    onDueAtChange = onDueAtChange
                )
                if (draft.title.isBlank()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Task title is required.",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = onSave,
                enabled = draft.canSave
            ) {
                Text("Save task")
            }
        },
        dismissButton = {
            TextButton(onClick = onCancel) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun TaskReviewChoiceRow(
    label: String,
    values: List<String>,
    selected: String,
    onSelected: (String) -> Unit
) {
    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(
        modifier = Modifier.horizontalScroll(rememberScrollState()),
        horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(8.dp)
    ) {
        values.forEach { value ->
            val isSelected = selected.equals(value, ignoreCase = true)
            Box(
                modifier = Modifier
                    .clip(MaterialTheme.shapes.large)
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onSelected(value) }
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            ) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.labelMedium,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TaskReviewDeadlineSelector(
    dueAt: Long?,
    onDueAtChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    val formattedDueAt = dueAt?.let {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))
    } ?: "No deadline"

    Text(
        text = "Deadline",
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = formattedDueAt,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    Row(horizontalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(10.dp)) {
        Button(
            onClick = {
                val initial = Calendar.getInstance().apply {
                    timeInMillis = dueAt ?: System.currentTimeMillis()
                }
                DatePickerDialog(
                    context,
                    { _, year, month, day ->
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                val selected = Calendar.getInstance().apply {
                                    set(Calendar.YEAR, year)
                                    set(Calendar.MONTH, month)
                                    set(Calendar.DAY_OF_MONTH, day)
                                    set(Calendar.HOUR_OF_DAY, hour)
                                    set(Calendar.MINUTE, minute)
                                    set(Calendar.SECOND, 0)
                                    set(Calendar.MILLISECOND, 0)
                                }
                                onDueAtChange(selected.timeInMillis)
                            },
                            initial.get(Calendar.HOUR_OF_DAY),
                            initial.get(Calendar.MINUTE),
                            true
                        ).show()
                    },
                    initial.get(Calendar.YEAR),
                    initial.get(Calendar.MONTH),
                    initial.get(Calendar.DAY_OF_MONTH)
                ).apply {
                    datePicker.minDate = System.currentTimeMillis()
                }.show()
            }
        ) {
            Text(if (dueAt == null) "Set" else "Change")
        }
        if (dueAt != null) {
            OutlinedButton(onClick = { onDueAtChange(null) }) {
                Text("Clear")
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AiResultScreenPreview() {
    VoiceToTaskTheme {
        AiResultScreen(
            uiState = AiResultUiState(
                transcript = "We discussed the upcoming launch and assigned tasks for the marketing team.",
                meetingSummary = "The team aligned on the launch plan and ownership.",
                decisionsText = "Launch on Friday\nUse the existing campaign assets",
                blockersText = "Final client approval is pending",
                followUpsText = "Confirm the final campaign budget",
                tasks = listOf(
                    TaskModel("1", "Setup social media ads", "Marketing", false),
                    TaskModel("2", "Draft email newsletter", "Marketing", false),
                    TaskModel("3", "Follow up with influencers", "PR", true)
                )
            ),
            onBackClick = {},
            onSaveTasksClick = {},
            onDiscardClick = {},
            onStayClick = {},
            onMeetingSummaryChange = {},
            onDecisionsChange = {},
            onBlockersChange = {},
            onFollowUpsChange = {},
            onAddTaskClick = {},
            onTaskDismiss = {},
            onTaskClick = {},
            onTaskCheckedChange = { _, _ -> },
            onCancelTaskDelete = {},
            onConfirmTaskDelete = {},
            onTaskTitleChange = {},
            onTaskDescriptionChange = {},
            onTaskAssigneeChange = {},
            onTaskDueAtChange = {},
            onTaskCategoryChange = {},
            onTaskPriorityChange = {},
            onSaveTaskEdit = {},
            onCancelTaskEdit = {}
        )
    }
}
