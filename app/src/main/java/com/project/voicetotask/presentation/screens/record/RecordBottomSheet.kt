package com.project.voicetotask.presentation.screens.record

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.google.accompanist.permissions.ExperimentalPermissionsApi
import com.google.accompanist.permissions.isGranted
import com.google.accompanist.permissions.rememberPermissionState
import com.project.voicetotask.R
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
    onUploadClick: () -> Unit,
    onDismissRequest: () -> Unit,
    modifier: Modifier = Modifier,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    val permissionState = rememberPermissionState(
        permission = android.Manifest.permission.RECORD_AUDIO
    )

    // Calculate a pulse scale based on amplitude (max is usually around 32767)
    val animatedScale by animateFloatAsState(
        targetValue = if (uiState.isRecording) 1f + (amplitude.toFloat() / 32767f) * 1.5f else 1f,
        animationSpec = tween(durationMillis = 100),
        label = "AmplitudePulse"
    )

    ModalBottomSheet(
        onDismissRequest = onDismissRequest,
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
                IconButton(onClick = onDismissRequest) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
            
            // Timer
            Text(
                text = uiState.durationText,
                style = MaterialTheme.typography.displayLarge,
                color = MaterialTheme.colorScheme.onSurface
            )

            if (uiState.errorMessage != null) {
                Text(
                    text = uiState.errorMessage,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }

            if (uiState.isProcessing) {
                Text(
                    text = "Processing audio...",
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
            
            Spacer(modifier = Modifier.height(48.dp))
            
            // Record Button
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
                    .size(96.dp * animatedScale) // Pulse effect
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
            onUploadClick = {},
            onDismissRequest = {}
        )
    }
}
