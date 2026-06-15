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
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
    onAvatarClick: () -> Unit,
    onStatsTaskClick: () -> Unit,
    onStatsMeetingClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest
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
                    TaskStatsRow(
                        tasksCount = uiState.tasksToDoTodayCount,
                        meetingsCount = uiState.recordedMeetingsCount,
                        onStatsTaskClick = onStatsTaskClick,
                        onStatsMeetingClick = onStatsMeetingClick
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
                    HomeCategories(
                        activeCategory = uiState.activeCategory,
                        onCategorySelected = onCategorySelected
                    )
                    Spacer(modifier = Modifier.height(24.dp))
                }

                item {
                    Text(
                        text = stringResource(id = R.string.task_list),
                        style = MaterialTheme.typography.headlineMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                }

                items(uiState.recentTasks) { task ->
                    TaskCard(
                        title = task.title,
                        category = task.category,
                        isCompleted = task.isCompleted,
                        onCheckedChange = { onTaskCheckedChange(task.id, it) },
                        onClick = { onTaskClick(task.id) }
                    )
                    Spacer(modifier = Modifier.height(12.dp))
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
private fun TaskStatsRow(
    tasksCount: Int,
    meetingsCount: Int,
    onStatsTaskClick: () -> Unit,
    onStatsMeetingClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        HomeStatCard(
            title = tasksCount.toString(),
            subtitle = stringResource(id = R.string.tasks_to_do_today),
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            contentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            onClick = onStatsTaskClick,
            modifier = Modifier.weight(1f)
        )
        
        HomeStatCard(
            title = meetingsCount.toString(),
            subtitle = stringResource(id = R.string.recorded_meetings),
            containerColor = MaterialTheme.colorScheme.secondaryContainer,
            contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
            onClick = onStatsMeetingClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HomeStatCard(
    title: String,
    subtitle: String,
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
private fun HomeCategories(
    activeCategory: String,
    onCategorySelected: (String) -> Unit
) {
    val categories = listOf(
        stringResource(id = R.string.see_all),
        stringResource(id = R.string.category_work),
        stringResource(id = R.string.category_meeting),
        stringResource(id = R.string.category_personal)
    )
    
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        categories.forEach { category ->
            val isSelected = category == activeCategory
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onCategorySelected(category) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = category,
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
                tasksToDoTodayCount = 5,
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
            onAvatarClick = {},
            onStatsTaskClick = {},
            onStatsMeetingClick = {}
        )
    }
}
