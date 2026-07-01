package com.project.voicetotask.presentation.screens.history

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.project.voicetotask.R
import com.project.voicetotask.presentation.components.TaskCard
import com.project.voicetotask.presentation.screens.task.TaskModel
import com.project.voicetotask.ui.theme.VoiceToTaskTheme
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MeetingDetailScreen(
    uiState: MeetingDetailUiState,
    onBackClick: () -> Unit,
    onShareClick: () -> Unit,
    onShareTranscript: () -> Unit,
    onShareMeetingNotes: () -> Unit,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit,
    onCopyTranscript: () -> Unit,
    onCopySummary: () -> Unit,
    onCopyMeetingNotes: () -> Unit,
    onExportTranscript: () -> Unit,
    onExportMeetingNotes: () -> Unit,
    onExportLinkedTasks: () -> Unit,
    onTranscriptSearchChange: (String) -> Unit,
    onTaskCheckedChange: (String, Boolean) -> Unit,
    onAddTaskClick: () -> Unit,
    onTaskClick: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
        topBar = {
            TopAppBar(
                title = { 
                    Text(
                        stringResource(id = R.string.meeting_details),
                        style = MaterialTheme.typography.titleLarge
                    ) 
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(imageVector = Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = onShareClick) {
                        Icon(imageVector = Icons.Default.Share, contentDescription = "Share")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            item {
                MeetingSummaryCard(
                    title = uiState.title,
                    date = uiState.date,
                    duration = uiState.duration,
                    onCopySummary = onCopySummary,
                    onCopyMeetingNotes = onCopyMeetingNotes,
                    onShareMeetingNotes = onShareMeetingNotes,
                    onExportMeetingNotes = onExportMeetingNotes,
                    onExportLinkedTasks = onExportLinkedTasks
                )
            }

            if (uiState.summary.isNotBlank()) {
                item {
                    MeetingIntelligenceCard(
                        title = stringResource(id = R.string.meeting_summary),
                        content = uiState.summary
                    )
                }
            }

            if (uiState.decisionsText.isNotBlank()) {
                item {
                    MeetingIntelligenceCard(
                        title = stringResource(id = R.string.key_decisions),
                        content = uiState.decisionsText
                    )
                }
            }

            if (uiState.blockersText.isNotBlank()) {
                item {
                    MeetingIntelligenceCard(
                        title = stringResource(id = R.string.blockers_risks),
                        content = uiState.blockersText
                    )
                }
            }

            if (uiState.followUpsText.isNotBlank()) {
                item {
                    MeetingIntelligenceCard(
                        title = stringResource(id = R.string.follow_ups),
                        content = uiState.followUpsText
                    )
                }
            }

            item {
                if (!uiState.isAudioAvailable) {
                    MissingAudioCard()
                } else {
                    AudioPlayerCard(
                        isPlaying = uiState.playerState.isPlaying,
                        currentPosition = uiState.playerState.currentPosition,
                        totalDuration = uiState.playerState.totalDuration,
                        playbackSpeed = uiState.playerState.playbackSpeed,
                        onPlayPauseClick = onPlayPauseClick,
                        onSeek = onSeek,
                        onPlaybackSpeedChange = onPlaybackSpeedChange
                    )
                }
            }

            item {
                TranscriptCard(
                    transcript = uiState.transcript,
                    searchQuery = uiState.transcriptSearchQuery,
                    matchCount = uiState.transcriptMatchCount,
                    onSearchQueryChange = onTranscriptSearchChange,
                    onCopyClick = onCopyTranscript,
                    onShareClick = onShareTranscript,
                    onExportClick = onExportTranscript
                )
            }

            item {
                Text(
                    text = stringResource(id = R.string.tasks_from_meeting),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            items(uiState.tasks) { task ->
                TaskCard(
                    title = task.title,
                    category = task.category,
                    isCompleted = task.isCompleted,
                    assigneeName = task.assigneeName,
                    dueAt = task.dueAt,
                    reminderTime = task.reminderTime,
                    priority = task.priority,
                    onCheckedChange = { onTaskCheckedChange(task.id, it) },
                    onClick = { onTaskClick(task.id) }
                )
            }

            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable(onClick = onAddTaskClick)
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = stringResource(id = R.string.add_task),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
private fun MeetingIntelligenceCard(
    title: String,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(14.dp))
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
}

@Composable
private fun MissingAudioCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = Icons.Default.PlayArrow,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text("Audio is unavailable", style = MaterialTheme.typography.titleMedium)
            Text(
                "Transcript and linked tasks are still available.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun MeetingSummaryCard(
    title: String,
    date: String,
    duration: String,
    onCopySummary: () -> Unit,
    onCopyMeetingNotes: () -> Unit,
    onShareMeetingNotes: () -> Unit,
    onExportMeetingNotes: () -> Unit,
    onExportLinkedTasks: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Text(
                text = title.ifBlank { stringResource(id = R.string.meeting_details) },
                style = MaterialTheme.typography.titleLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = listOf(date, duration)
                    .filter { it.isNotBlank() }
                    .joinToString(" - "),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onCopySummary) {
                    Icon(
                        imageVector = Icons.Default.ContentCopy,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Copy summary")
                }
                TextButton(onClick = onCopyMeetingNotes) {
                    Text("Copy notes")
                }
                TextButton(onClick = onShareMeetingNotes) {
                    Icon(
                        imageVector = Icons.Default.Share,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Share notes")
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                TextButton(onClick = onExportMeetingNotes) {
                    Text("Export notes .txt")
                }
                TextButton(onClick = onExportLinkedTasks) {
                    Text("Export tasks .csv")
                }
            }
        }
    }
}

@Composable
private fun AudioPlayerCard(
    isPlaying: Boolean,
    currentPosition: Long,
    totalDuration: Long,
    playbackSpeed: Float,
    onPlayPauseClick: () -> Unit,
    onSeek: (Long) -> Unit,
    onPlaybackSpeedChange: (Float) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
                    .clickable(onClick = onPlayPauseClick),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                    contentDescription = if (isPlaying) "Pause" else "Play",
                    tint = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(24.dp))
            
            Slider(
                value = if (totalDuration > 0) currentPosition.toFloat() / totalDuration else 0f,
                onValueChange = { onSeek((it * totalDuration).toLong()) },
                colors = SliderDefaults.colors(
                    thumbColor = MaterialTheme.colorScheme.primary,
                    activeTrackColor = MaterialTheme.colorScheme.primary,
                    inactiveTrackColor = MaterialTheme.colorScheme.surfaceVariant
                )
            )
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = formatDuration(currentPosition),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = formatDuration(totalDuration),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                listOf(0.75f, 1f, 1.25f, 1.5f).forEach { speed ->
                    val selected = kotlin.math.abs(playbackSpeed - speed) < 0.01f
                    AssistChip(
                        onClick = { onPlaybackSpeedChange(speed) },
                        label = { Text("${formatSpeed(speed)}x") },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = if (selected) {
                                MaterialTheme.colorScheme.primaryContainer
                            } else {
                                MaterialTheme.colorScheme.surfaceContainer
                            },
                            labelColor = if (selected) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun TranscriptCard(
    transcript: String,
    searchQuery: String,
    matchCount: Int,
    onSearchQueryChange: (String) -> Unit,
    onCopyClick: () -> Unit,
    onShareClick: () -> Unit,
    onExportClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(24.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.transcript),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Row {
                    IconButton(onClick = onCopyClick) {
                        Icon(
                            imageVector = Icons.Default.ContentCopy,
                            contentDescription = "Copy",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onShareClick) {
                        Icon(
                            imageVector = Icons.Default.Share,
                            contentDescription = "Share transcript",
                            modifier = Modifier.size(20.dp),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    TextButton(onClick = onExportClick) {
                        Text("Export")
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = searchQuery,
                onValueChange = onSearchQueryChange,
                label = { Text("Search transcript") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Search, contentDescription = null)
                },
                supportingText = {
                    val message = when {
                        searchQuery.isBlank() -> "Search inside this transcript."
                        matchCount == 0 -> "No matches found."
                        matchCount == 1 -> "1 match found."
                        else -> "$matchCount matches found."
                    }
                    Text(message)
                },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(16.dp))
            
            Text(
                text = highlightedTranscript(
                    transcript = transcript.ifBlank { "Transcript is unavailable." },
                    query = searchQuery
                ),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight * 1.5
            )
        }
    }
}

private fun formatDuration(millis: Long): String {
    val totalSeconds = millis / 1000
    val minutes = totalSeconds / 60
    val seconds = totalSeconds % 60
    return String.format(Locale.getDefault(), "%02d:%02d", minutes, seconds)
}

private fun formatSpeed(speed: Float): String {
    return if (speed % 1f == 0f) {
        speed.toInt().toString()
    } else {
        speed.toString()
    }
}

@Composable
private fun highlightedTranscript(
    transcript: String,
    query: String
) = buildAnnotatedString {
    val normalizedQuery = query.trim()
    if (normalizedQuery.isBlank()) {
        append(transcript)
        return@buildAnnotatedString
    }

    val matches = Regex(Regex.escape(normalizedQuery), RegexOption.IGNORE_CASE)
        .findAll(transcript)
        .toList()
    if (matches.isEmpty()) {
        append(transcript)
        return@buildAnnotatedString
    }

    var cursor = 0
    matches.forEach { match ->
        append(transcript.substring(cursor, match.range.first))
        pushStyle(
            SpanStyle(
                background = MaterialTheme.colorScheme.primaryContainer,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                fontWeight = FontWeight.SemiBold
            )
        )
        append(match.value)
        pop()
        cursor = match.range.last + 1
    }
    append(transcript.substring(cursor))
}

@Preview(showBackground = true)
@Composable
fun MeetingDetailScreenPreview() {
    VoiceToTaskTheme {
        MeetingDetailScreen(
            uiState = MeetingDetailUiState(
                title = "Chi tiết cuộc họp",
                date = "12/10/2023 14:30",
                transcript = "Speaker 1: Chào mọi người, chúng ta bắt đầu buổi họp hôm nay nhé. Mục tiêu chính là chốt lại các tính năng cho phiên bản MVP sắp tới.",
                tasks = listOf(
                    TaskModel("1", "Kiểm tra lỗi permission chia sẻ file trên Android 13", "Work", false),
                    TaskModel("2", "Review lại Design System cho màn hình Settings", "Work", false)
                )
            ),
            onBackClick = {},
            onShareClick = {},
            onShareTranscript = {},
            onShareMeetingNotes = {},
            onPlayPauseClick = {},
            onSeek = {},
            onPlaybackSpeedChange = {},
            onCopyTranscript = {},
            onCopySummary = {},
            onCopyMeetingNotes = {},
            onExportTranscript = {},
            onExportMeetingNotes = {},
            onExportLinkedTasks = {},
            onTranscriptSearchChange = {},
            onTaskCheckedChange = { _, _ -> },
            onAddTaskClick = {},
            onTaskClick = {}
        )
    }
}
