package com.project.voicetotask.presentation.screens.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.InputField
import com.project.voicetotask.presentation.components.TaskCard
import com.project.voicetotask.ui.theme.VoiceToTaskTheme

@Composable
fun HomeScreen(
    uiState: HomeUiState,
    onSearchQueryChange: (String) -> Unit,
    onCategorySelected: (String) -> Unit,
    onTaskClick: (String) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    onTaskDelete: (String) -> Unit,
    onAvatarClick: () -> Unit,
    onStatsTaskClick: () -> Unit,
    onStatsMeetingClick: () -> Unit,
    onAddTaskClick: () -> Unit,
    onRecordClick: () -> Unit,
    onExportTasksClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    var taskPendingDelete by remember { mutableStateOf<TaskModel?>(null) }

    taskPendingDelete?.let { task ->
        AlertDialog(
            onDismissRequest = { taskPendingDelete = null },
            title = { Text("Delete task?") },
            text = { Text("This task will be removed permanently.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onTaskDelete(task.id)
                        taskPendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { taskPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        floatingActionButton = {
            FloatingActionButton(
                onClick = onRecordClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary
            ) {
                Icon(imageVector = Icons.Default.Mic, contentDescription = "Record")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            HomeHeader(
                userName = uiState.userName,
                onAvatarClick = onAvatarClick,
                modifier = Modifier.padding(horizontal = 24.dp, vertical = 16.dp)
            )
            
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(8.dp))
                    TaskStatsSection(
                        pendingCount = uiState.pendingTaskCount,
                        completedCount = uiState.completedTaskCount,
                        overdueCount = uiState.overdueTaskCount,
                        upcomingCount = uiState.upcomingTaskCount,
                        meetingsCount = uiState.recordedMeetingsCount,
                        onStatsTaskClick = onStatsTaskClick,
                        onStatsMeetingClick = onStatsMeetingClick
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    TaskIntelligenceSummary(
                        myTasks = uiState.myTaskCount,
                        unassignedTasks = uiState.unassignedTaskCount,
                        highPriorityTasks = uiState.highPriorityTaskCount,
                        meetingTasks = uiState.meetingLinkedTaskCount
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    HomeSearchBar(
                        searchQuery = uiState.searchQuery,
                        onSearchQueryChange = onSearchQueryChange
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    DashboardFilters(
                        activeFilter = uiState.activeFilter,
                        onCategorySelected = onCategorySelected
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = stringResource(id = R.string.task_list),
                            style = MaterialTheme.typography.headlineMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            TextButton(onClick = onExportTasksClick) {
                                Text("Export CSV")
                            }
                            IconButton(onClick = onAddTaskClick) {
                                Icon(imageVector = Icons.Default.Add, contentDescription = "Add task")
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(16.dp))
                }

                if (uiState.recentTasks.isEmpty()) {
                    item {
                        EmptyTaskState(
                            message = uiState.emptyMessage,
                            onAddTaskClick = onAddTaskClick
                        )
                    }
                }

                items(uiState.recentTasks, key = { it.id }) { task ->
                    val dismissState = rememberSwipeToDismissBoxState(
                        confirmValueChange = { value ->
                            if (value == SwipeToDismissBoxValue.EndToStart) {
                                taskPendingDelete = task
                            }
                            false
                        }
                    )
                    SwipeToDismissBox(
                        state = dismissState,
                        enableDismissFromStartToEnd = false,
                        backgroundContent = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(vertical = 6.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(MaterialTheme.colorScheme.errorContainer),
                                contentAlignment = Alignment.CenterEnd
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Delete,
                                    contentDescription = "Delete task",
                                    tint = MaterialTheme.colorScheme.onErrorContainer,
                                    modifier = Modifier.padding(end = 20.dp)
                                )
                            }
                        }
                    ) {
                        TaskCard(
                            title = task.title,
                            category = task.category,
                            isCompleted = task.isCompleted,
                            assigneeName = task.assigneeName,
                            dueAt = task.dueAt,
                            reminderTime = task.reminderTime,
                            priority = task.priority,
                            meetingLinked = task.meetingId != null,
                            deadlineStatus = task.deadlineStatus,
                            priorityColor = priorityColor(task.priorityCode),
                            onCheckedChange = { onTaskCheckedChange(task.id, it) },
                            onClick = { onTaskClick(task.id) },
                            modifier = Modifier.padding(vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeHeader(
    userName: String,
    onAvatarClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.good_morning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Text(
                text = userName,
                style = MaterialTheme.typography.headlineLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }

        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer)
                .clickable(onClick = onAvatarClick),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.Person,
                contentDescription = "Profile",
                tint = MaterialTheme.colorScheme.onPrimaryContainer
            )
        }
    }
}

@Composable
private fun TaskStatsSection(
    pendingCount: Int,
    completedCount: Int,
    overdueCount: Int,
    upcomingCount: Int,
    meetingsCount: Int,
    onStatsTaskClick: () -> Unit,
    onStatsMeetingClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeStatCard(
            title = overdueCount.toString(),
            subtitle = "Overdue",
            icon = Icons.Default.Schedule,
            containerColor = MaterialTheme.colorScheme.errorContainer,
            contentColor = MaterialTheme.colorScheme.onErrorContainer,
            onClick = onStatsTaskClick,
            modifier = Modifier.weight(1f)
        )

        HomeStatCard(
            title = upcomingCount.toString(),
            subtitle = "Upcoming 7 days",
            icon = Icons.Default.CheckCircle,
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onStatsTaskClick,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(12.dp))
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeStatCard(
            title = pendingCount.toString(),
            subtitle = "Pending",
            icon = Icons.Default.Schedule,
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onStatsTaskClick,
            modifier = Modifier.weight(1f)
        )
        HomeStatCard(
            title = meetingsCount.toString(),
            subtitle = stringResource(id = R.string.recorded_meetings),
            icon = Icons.Default.Groups,
            containerColor = MaterialTheme.colorScheme.tertiaryContainer,
            contentColor = MaterialTheme.colorScheme.onTertiaryContainer,
            onClick = onStatsMeetingClick,
            modifier = Modifier.weight(1f)
        )
    }
    Spacer(modifier = Modifier.height(8.dp))
    Text(
        text = "$completedCount completed total",
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun TaskIntelligenceSummary(
    myTasks: Int,
    unassignedTasks: Int,
    highPriorityTasks: Int,
    meetingTasks: Int
) {
    Text(
        text = "Task intelligence",
        style = MaterialTheme.typography.titleMedium,
        color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(modifier = Modifier.height(10.dp))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        IntelligencePill(label = "My tasks", count = myTasks)
        IntelligencePill(label = "Unassigned", count = unassignedTasks)
        IntelligencePill(label = "High priority", count = highPriorityTasks)
        IntelligencePill(label = "From meetings", count = meetingTasks)
    }
}

@Composable
private fun IntelligencePill(
    label: String,
    count: Int
) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = count.toString(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.size(8.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    subtitle: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    containerColor: Color,
    contentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = containerColor),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.displayLarge,
                color = contentColor
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = contentColor
            )
        }
    }
}

@Composable
private fun EmptyTaskState(
    message: String,
    onAddTaskClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(28.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.CheckCircle,
                contentDescription = null,
                modifier = Modifier.size(44.dp),
                tint = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(message, style = MaterialTheme.typography.titleMedium)
            Text(
                "Change the filter or create a new task.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TextButton(onClick = onAddTaskClick) {
                Text("Create task")
            }
        }
    }
}

@Composable
private fun priorityColor(priorityCode: Int): Color {
    return when (priorityCode) {
        2 -> MaterialTheme.colorScheme.error
        1 -> MaterialTheme.colorScheme.tertiary
        else -> MaterialTheme.colorScheme.primary
    }
}

@Composable
private fun HomeSearchBar(
    searchQuery: String,
    onSearchQueryChange: (String) -> Unit
) {
    InputField(
        value = searchQuery,
        onValueChange = onSearchQueryChange,
        label = stringResource(id = R.string.search_tasks),
        leadingIcon = Icons.Default.Search
    )
}

@Composable
private fun DashboardFilters(
    activeFilter: TaskDashboardFilter,
    onCategorySelected: (String) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TaskDashboardFilter.entries.forEach { filter ->
            val isSelected = filter == activeFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onCategorySelected(filter.label) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimary
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    VoiceToTaskTheme {
        HomeScreen(
            uiState = HomeUiState(
                userName = "Thien",
                pendingTaskCount = 5,
                completedTaskCount = 3,
                overdueTaskCount = 2,
                upcomingTaskCount = 4,
                myTaskCount = 3,
                unassignedTaskCount = 0,
                highPriorityTaskCount = 2,
                meetingLinkedTaskCount = 2,
                recordedMeetingsCount = 2,
                recentTasks = listOf(
                    TaskModel("1", "Prepare Q3 Presentation", "Work", false),
                    TaskModel("2", "Buy groceries", "Personal", true)
                )
            ),
            onSearchQueryChange = {},
            onCategorySelected = {},
            onTaskClick = {},
            onTaskCheckedChange = { _, _ -> },
            onTaskDelete = {},
            onAvatarClick = {},
            onStatsTaskClick = {},
            onStatsMeetingClick = {},
            onAddTaskClick = {},
            onRecordClick = {},
            onExportTasksClick = {}
        )
    }
}
