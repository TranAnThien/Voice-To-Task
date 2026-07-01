package com.project.voicetotask.presentation.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.TextButton
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.project.voicetotask.R
import com.project.voicetotask.domain.model.AiPromptProfile
import com.project.voicetotask.presentation.components.PrimaryButton

import androidx.compose.ui.tooling.preview.Preview
import com.project.voicetotask.ui.theme.VoiceToTaskTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalPermissionsApi::class)
@Composable
fun RecordBottomSheet(
    uiState: RecordUiState,
    amplitude: Int,
    onStartRecording: () -> Unit,
    onStopRecording: () -> Unit,
    onPauseResume: () -> Unit,
    onCancelRecording: () -> Unit,
    onRetryProcessing: () -> Unit,
    onDeleteCurrentAudio: () -> Unit,
    onPromptProfileSelected: (AiPromptProfile) -> Unit,
    onUploadClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    var showCancelDialog by remember { mutableStateOf(false) }
    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.RECORD_AUDIO
    )

    // Calculate a pulse scale based on amplitude (max is usually around 32767)
    val animatedScale by animateFloatAsState(
        targetValue = if (uiState.isRecording && !uiState.isPaused) {
            1f + (amplitude.toFloat() / 32767f) * 1.5f
        } else {
            1f
        },
        animationSpec = tween(durationMillis = 100),
        label = "AmplitudePulse"
    )

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text("Discard recording?") },
            text = { Text("The current recording will be deleted.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        onCancelRecording()
                        showCancelDialog = false
                        onDismissRequest()
                    }
                ) {
                    Text("Discard")
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text("Keep recording")
                }
            }
        )
    }

    ModalBottomSheet(
        onDismissRequest = {
            if (uiState.isRecording) showCancelDialog = true
            else if (!uiState.isProcessing) onDismissRequest()
        },
        sheetState = sheetState,
        dragHandle = { BottomSheetDefaults.DragHandle() },
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(id = R.string.recording_task),
                    style = MaterialTheme.typography.titleLarge
                )
                IconButton(
                    onClick = {
                        if (uiState.isRecording) showCancelDialog = true
                        else if (!uiState.isProcessing) onDismissRequest()
                    }
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))

            PromptProfileSelector(
                selectedProfile = uiState.selectedPromptProfile,
                profiles = uiState.availablePromptProfiles,
                enabled = !uiState.isRecording && !uiState.isProcessing,
                onPromptProfileSelected = onPromptProfileSelected
            )

            Spacer(modifier = Modifier.height(24.dp))
            
            // Timer
            Text(
                text = uiState.durationText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = when {
                    uiState.isProcessing -> "Analyzing audio"
                    uiState.isPaused -> "Recording paused"
                    uiState.isRecording -> "Listening..."
                    uiState.errorMessage != null -> "Action needed"
                    else -> "Ready to record"
                },
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!permissionState.status.isGranted && !uiState.isRecording) {
                Text(
                    text = "Microphone permission is required to record audio.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.qualityWarning != null) {
                Text(
                    text = uiState.qualityWarning,
                    color = MaterialTheme.colorScheme.tertiary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (
                uiState.errorMessage != null &&
                (uiState.canRetryProcessing || uiState.canDeleteCurrentAudio)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    if (uiState.canRetryProcessing) {
                        OutlinedButton(
                            onClick = onRetryProcessing,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Retry")
                        }
                    }
                    if (uiState.canDeleteCurrentAudio) {
                        OutlinedButton(
                            onClick = onDeleteCurrentAudio,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("Delete audio")
                        }
                    }
                }
            }

            if (uiState.isProcessing) {
                CircularProgressIndicator(modifier = Modifier.padding(top = 20.dp))
            }
            
            Spacer(modifier = Modifier.height(28.dp))
            AudioWaveform(
                amplitude = amplitude,
                active = uiState.isRecording && !uiState.isPaused
            )
            Spacer(modifier = Modifier.height(32.dp))
            
            Row(
                horizontalArrangement = Arrangement.spacedBy(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (uiState.isRecording) {
                    IconButton(
                        onClick = onPauseResume,
                        enabled = !uiState.isProcessing,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.secondaryContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = if (uiState.isPaused) {
                                Icons.Default.PlayArrow
                            } else {
                                Icons.Default.Pause
                            },
                            contentDescription = if (uiState.isPaused) "Resume" else "Pause"
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                        .background(
                            if (uiState.isRecording) MaterialTheme.colorScheme.errorContainer
                            else MaterialTheme.colorScheme.primaryContainer
                        )
                        .border(
                            width = 4.dp,
                            color = if (uiState.isRecording) {
                                MaterialTheme.colorScheme.error.copy(alpha = 0.5f)
                            } else {
                                Color.Transparent
                            },
                            shape = CircleShape
                        )
                        .size(96.dp * animatedScale)
                        .clickable {
                            if (uiState.isProcessing) return@clickable
                            if (permissionState.status.isGranted) {
                                if (uiState.isRecording) onStopRecording() else onStartRecording()
                            } else {
                                permissionState.launchPermissionRequest()
                            }
                        },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = if (uiState.isRecording) Icons.Default.Stop else Icons.Default.Mic,
                        contentDescription = "Record",
                        modifier = Modifier.size(48.dp),
                        tint = if (uiState.isRecording) MaterialTheme.colorScheme.onErrorContainer
                        else MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
                if (uiState.isRecording) {
                    IconButton(
                        onClick = { showCancelDialog = true },
                        enabled = !uiState.isProcessing,
                        modifier = Modifier
                            .size(56.dp)
                            .background(
                                MaterialTheme.colorScheme.errorContainer,
                                CircleShape
                            )
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Discard recording",
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Upload Option
            OutlinedButton(
                onClick = onUploadClick,
                enabled = !uiState.isRecording && !uiState.isProcessing,
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.extraLarge
            ) {
                Text(text = stringResource(id = R.string.upload_audio_file))
            }
            
            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun AudioWaveform(amplitude: Int, active: Boolean) {
    val normalized = if (active) {
        (amplitude.toFloat() / 32767f).coerceIn(0.08f, 1f)
    } else {
        0.08f
    }
    val color = MaterialTheme.colorScheme.primary
    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
    ) {
        val bars = 28
        val spacing = size.width / bars
        repeat(bars) { index ->
            val rhythm = 0.35f + ((index % 7) / 10f)
            val height = size.height * normalized * rhythm
            drawLine(
                color = color,
                start = androidx.compose.ui.geometry.Offset(
                    x = spacing * index + spacing / 2,
                    y = size.height / 2 - height / 2
                ),
                end = androidx.compose.ui.geometry.Offset(
                    x = spacing * index + spacing / 2,
                    y = size.height / 2 + height / 2
                ),
                strokeWidth = 5.dp.toPx(),
                cap = StrokeCap.Round
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Preview(showBackground = true)
@Composable
fun RecordBottomSheetPreview() {
    VoiceToTaskTheme {
        RecordBottomSheet(
            uiState = RecordUiState(
                isRecording = true,
                durationText = "02:45"
            ),
            amplitude = 15000,
            onStartRecording = {},
            onStopRecording = {},
            onPauseResume = {},
            onCancelRecording = {},
            onRetryProcessing = {},
            onDeleteCurrentAudio = {},
            onPromptProfileSelected = {},
            onUploadClick = {},
            onDismissRequest = {}
        )
    }
}

@Composable
private fun PromptProfileSelector(
    selectedProfile: AiPromptProfile,
    profiles: List<AiPromptProfile>,
    enabled: Boolean,
    onPromptProfileSelected: (AiPromptProfile) -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "AI analysis profile",
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(8.dp))
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            profiles.forEach { profile ->
                val selected = profile == selectedProfile
                AssistChip(
                    onClick = { if (enabled) onPromptProfileSelected(profile) },
                    label = { Text(profile.displayName) },
                    enabled = enabled,
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
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = selectedProfile.description,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
