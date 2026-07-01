package com.project.voicetotask.presentation.screens.task

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.InputField
import com.project.voicetotask.presentation.components.PrimaryButton
import com.project.voicetotask.ui.theme.VoiceToTaskTheme
import java.text.DateFormat
import java.util.Calendar
import java.util.Date

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskDetailScreen(
    uiState: TaskDetailUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onCategoryChange: (String) -> Unit,
    onPriorityChange: (String) -> Unit,
    onAssigneeChange: (String) -> Unit,
    onDueAtChange: (Long?) -> Unit,
    onCompletedChange: (Boolean) -> Unit,
    onReminderChange: (Long?) -> Unit,
    onSaveClick: () -> Unit,
    onDeleteClick: () -> Unit,
    onBackClick: () -> Unit,
    showDeleteButton: Boolean,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.task_details_title),
                        style = MaterialTheme.typography.headlineMedium
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    if (showDeleteButton) {
                        IconButton(onClick = onDeleteClick) {
                            Icon(imageVector = Icons.Default.Delete, contentDescription = "Delete")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
                )
            )
        },
        bottomBar = {
            TaskDetailBottomBar(
                onSaveClick = onSaveClick,
                enabled = uiState.canSave && !uiState.isLoading
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            TaskSection(title = "Task information") {
                TaskDetailForm(
                    uiState = uiState,
                    onTitleChange = onTitleChange,
                    onNotesChange = onNotesChange,
                    onAssigneeChange = onAssigneeChange
                )
            }
            uiState.errorMessage?.let { message ->
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TaskSection(title = "Classification") {
                TaskDetailCategorySelector(
                    selectedCategory = uiState.category,
                    onCategoryChange = onCategoryChange
                )
                Spacer(modifier = Modifier.height(20.dp))
                TaskDetailPrioritySelector(
                    selectedPriority = uiState.priority,
                    onPriorityChange = onPriorityChange
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TaskSection(title = "Schedule") {
                TaskDateTimeSelector(
                    label = "Deadline - completion due",
                    value = uiState.dueAt,
                    emptyText = "No deadline",
                    onValueChange = onDueAtChange
                )
                Spacer(modifier = Modifier.height(20.dp))
                TaskReminderSelector(
                    reminderTime = uiState.reminderTime,
                    onReminderChange = onReminderChange
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            TaskSection(title = "Status") {
                TaskDetailStatusToggle(
                    isCompleted = uiState.isCompleted,
                    onCompletedChange = onCompletedChange
                )
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun TaskSection(
    title: String,
    content: @Composable () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(16.dp))
            content()
        }
    }
}

@Composable
private fun TaskReminderSelector(
    reminderTime: Long?,
    onReminderChange: (Long?) -> Unit
) {
    TaskDateTimeSelector(
        label = "Reminder - notification time",
        value = reminderTime,
        emptyText = "No reminder",
        onValueChange = onReminderChange
    )
}

@Composable
private fun TaskDateTimeSelector(
    label: String,
    value: Long?,
    emptyText: String,
    onValueChange: (Long?) -> Unit
) {
    val context = LocalContext.current
    val formattedValue = value?.let {
        DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(Date(it))
    } ?: emptyText

    Text(
        text = label,
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = formattedValue,
        style = MaterialTheme.typography.bodyLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Button(
            onClick = {
                val initial = Calendar.getInstance().apply {
                    timeInMillis = value ?: System.currentTimeMillis()
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
                                onValueChange(selected.timeInMillis)
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
            Text(if (value == null) "Set" else "Change")
        }
        if (value != null) {
            OutlinedButton(onClick = { onValueChange(null) }) {
                Text("Clear")
            }
        }
    }
}

@Composable
private fun TaskDetailForm(
    uiState: TaskDetailUiState,
    onTitleChange: (String) -> Unit,
    onNotesChange: (String) -> Unit,
    onAssigneeChange: (String) -> Unit
) {
    InputField(
        value = uiState.title,
        onValueChange = onTitleChange,
        label = stringResource(id = R.string.task_name)
    )
    Spacer(modifier = Modifier.height(16.dp))
    InputField(
        value = uiState.notes,
        onValueChange = onNotesChange,
        label = stringResource(id = R.string.task_notes),
        modifier = Modifier.height(120.dp) // Taller input for notes
    )
    Spacer(modifier = Modifier.height(16.dp))
    InputField(
        value = uiState.assigneeName,
        onValueChange = onAssigneeChange,
        label = "Assignee"
    )
}

@Composable
private fun TaskDetailCategorySelector(
    selectedCategory: String,
    onCategoryChange: (String) -> Unit
) {
    Text(
        text = stringResource(id = R.string.category),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    val categories = listOf(
        stringResource(id = R.string.category_work),
        stringResource(id = R.string.category_meeting),
        stringResource(id = R.string.category_personal)
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == selectedCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onCategoryChange(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TaskDetailPrioritySelector(
    selectedPriority: String,
    onPriorityChange: (String) -> Unit
) {
    Text(
        text = stringResource(id = R.string.priority),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(8.dp))
    
    val priorities = listOf(
        stringResource(id = R.string.priority_high),
        stringResource(id = R.string.priority_medium),
        stringResource(id = R.string.priority_low)
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        priorities.forEach { priority ->
            val isSelected = priority == selectedPriority
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onPriorityChange(priority) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = priority,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun TaskDetailStatusToggle(
    isCompleted: Boolean,
    onCompletedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Checkbox(
            checked = isCompleted,
            onCheckedChange = onCompletedChange
        )
        Text(
            text = stringResource(id = R.string.completed),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun TaskDetailBottomBar(
    onSaveClick: () -> Unit,
    enabled: Boolean
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surfaceContainerLowest)
            .padding(24.dp)
    ) {
        PrimaryButton(
            text = stringResource(id = R.string.save_changes),
            onClick = onSaveClick,
            enabled = enabled
        )
    }
}

@Preview(showBackground = true)
@Composable
fun TaskDetailScreenPreview() {
    VoiceToTaskTheme {
        TaskDetailScreen(
            uiState = TaskDetailUiState(
                title = "Prepare Q3 Presentation",
                notes = "Need to discuss sales metrics.",
                category = "Work",
                priority = "High"
            ),
            onTitleChange = {},
            onNotesChange = {},
            onCategoryChange = {},
            onPriorityChange = {},
            onAssigneeChange = {},
            onDueAtChange = {},
            onCompletedChange = {},
            onReminderChange = {},
            onSaveClick = {},
            onDeleteClick = {},
            onBackClick = {},
            showDeleteButton = true
        )
    }
}
