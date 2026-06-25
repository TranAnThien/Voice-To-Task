package com.project.voicetotask.presentation.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.PrimaryButton
import com.project.voicetotask.presentation.components.TaskCard
import com.project.voicetotask.presentation.screens.task.TaskModel

import androidx.compose.ui.tooling.preview.Preview
import com.project.voicetotask.ui.theme.VoiceToTaskTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiResultScreen(
    uiState: AiResultUiState,
    onBackClick: () -> Unit,
    onSaveTasksClick: () -> Unit,
    onTaskDismiss: (TaskModel) -> Unit,
    onTaskClick: (TaskModel) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(id = R.string.tasks_from_meeting)) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        },
        bottomBar = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                PrimaryButton(
                    text = stringResource(id = R.string.save_changes),
                    onClick = onSaveTasksClick
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
                Text(
                    text = stringResource(id = R.string.transcript),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.summary,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(24.dp))
                
                HorizontalDivider()
                Spacer(modifier = Modifier.height(24.dp))
                
                Text(
                    text = stringResource(id = R.string.task_list),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.tasks.isEmpty()) {
                    Text(
                        text = "No tasks were detected from this transcript.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
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
                        onCheckedChange = { onTaskCheckedChange(task.id, it) },
                        onClick = { onTaskClick(task) },
                        modifier = Modifier.padding(vertical = 6.dp)
                    )
                }
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
                summary = "We discussed the upcoming launch and assigned tasks for the marketing team. John will handle social media ads, and Sarah will draft the email newsletter.",
                tasks = listOf(
                    TaskModel("1", "Setup social media ads", "Marketing", false),
                    TaskModel("2", "Draft email newsletter", "Marketing", false),
                    TaskModel("3", "Follow up with influencers", "PR", true)
                )
            ),
            onBackClick = {},
            onSaveTasksClick = {},
            onTaskDismiss = {},
            onTaskClick = {},
            onTaskCheckedChange = { _, _ -> }
        )
    }
}
