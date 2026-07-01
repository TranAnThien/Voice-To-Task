package com.project.voicetotask.presentation.screens.history

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.InputField
import com.project.voicetotask.presentation.components.MeetingCard
import androidx.compose.ui.tooling.preview.Preview
import com.project.voicetotask.ui.theme.VoiceToTaskTheme

@Composable
fun HistoryScreen(
    uiState: HistoryUiState,
    onSearchChange: (String) -> Unit,
    onFilterChange: (HistoryFilter) -> Unit,
    onMeetingClick: (MeetingModel) -> Unit,
    onExportBackupClick: () -> Unit,
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
            Spacer(modifier = Modifier.height(16.dp))
            HistoryHeader(
                onExportBackupClick = onExportBackupClick,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            HistorySearch(
                searchQuery = uiState.searchQuery,
                onSearchChange = onSearchChange,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            HistoryFilters(
                activeFilter = uiState.activeFilter,
                onFilterChange = onFilterChange,
                modifier = Modifier.padding(horizontal = 24.dp)
            )
            Spacer(modifier = Modifier.height(24.dp))
            HistoryList(
                meetings = uiState.meetings,
                emptyMessage = uiState.emptyMessage,
                onMeetingClick = onMeetingClick,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun HistoryHeader(
    onExportBackupClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(id = R.string.meeting_history),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        TextButton(onClick = onExportBackupClick) {
            Text("Backup JSON")
        }
    }
}

@Composable
private fun HistorySearch(
    searchQuery: String,
    onSearchChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(modifier = modifier) {
        InputField(
            value = searchQuery,
            onValueChange = onSearchChange,
            label = stringResource(id = R.string.search_meetings),
            leadingIcon = Icons.Default.Search
        )
    }
}

@Composable
private fun HistoryFilters(
    activeFilter: HistoryFilter,
    onFilterChange: (HistoryFilter) -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        HistoryFilter.entries.forEach { filter ->
            val isSelected = filter == activeFilter
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(16.dp))
                    .background(
                        if (isSelected) MaterialTheme.colorScheme.primaryContainer
                        else MaterialTheme.colorScheme.surfaceContainer
                    )
                    .clickable { onFilterChange(filter) }
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = filter.label,
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isSelected) MaterialTheme.colorScheme.onPrimaryContainer
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun HistoryList(
    meetings: List<MeetingModel>,
    emptyMessage: String,
    onMeetingClick: (MeetingModel) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier,
        contentPadding = PaddingValues(start = 24.dp, end = 24.dp, bottom = 24.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (meetings.isEmpty()) {
            item {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 48.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(emptyMessage, style = MaterialTheme.typography.titleMedium)
                    Text(
                        "Try another search or filter.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        items(meetings, key = { it.id }) { meeting ->
            MeetingCard(
                title = meeting.title,
                date = meeting.date,
                duration = meeting.duration,
                taskCount = meeting.taskCount,
                summaryPreview = meeting.summaryPreview,
                badges = meeting.badges,
                assigneePreview = meeting.assigneePreview,
                onClick = { onMeetingClick(meeting) }
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun HistoryScreenPreview() {
    VoiceToTaskTheme {
        HistoryScreen(
            uiState = HistoryUiState(
                meetings = listOf(
                    MeetingModel(
                        id = "1",
                        title = "Weekly Sync",
                        date = "Oct 12, 2023 09:00",
                        duration = "45:00",
                        taskCount = 5,
                        summaryPreview = "Reviewed launch scope and open blockers.",
                        badges = listOf("Decisions", "Blockers"),
                        assigneePreview = "Me, An"
                    ),
                    MeetingModel("2", "Project Kickoff", "Oct 11, 2023 10:00", "30:00", 3),
                    MeetingModel("3", "Client Interview", "Oct 10, 2023 14:00", "01:15:00", 2)
                ),
                activeFilter = HistoryFilter.ALL
            ),
            onSearchChange = {},
            onFilterChange = {},
            onMeetingClick = {},
            onExportBackupClick = {}
        )
    }
}
