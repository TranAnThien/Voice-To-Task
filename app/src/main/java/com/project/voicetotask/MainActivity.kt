package com.project.voicetotask

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat.enableEdgeToEdge
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.project.voicetotask.presentation.components.BottomNavigationBar
import com.project.voicetotask.data.reminder.TaskReminderContract
import com.project.voicetotask.presentation.export.ExportFormatter
import com.project.voicetotask.presentation.navigation.Screen
import com.project.voicetotask.presentation.screens.history.HistoryScreen
import com.project.voicetotask.presentation.screens.history.HistoryViewModel
import com.project.voicetotask.presentation.screens.history.MeetingDetailScreen
import com.project.voicetotask.presentation.screens.history.MeetingDetailViewModel
import com.project.voicetotask.presentation.screens.record.AiResultScreen
import com.project.voicetotask.presentation.screens.record.AiResultViewModel
import com.project.voicetotask.presentation.screens.record.RecordBottomSheet
import com.project.voicetotask.presentation.screens.record.RecordViewModel
import com.project.voicetotask.presentation.screens.settings.SettingsScreen
import com.project.voicetotask.presentation.screens.settings.SettingsViewModel
import com.project.voicetotask.presentation.screens.task.HomeScreen
import com.project.voicetotask.presentation.screens.task.HomeViewModel
import com.project.voicetotask.presentation.screens.task.TaskDetailScreen
import com.project.voicetotask.presentation.screens.task.TaskDetailViewModel
import com.project.voicetotask.presentation.screens.task.TaskDetailViewModel.Companion.NEW_TASK_ID
import dagger.hilt.android.AndroidEntryPoint
import com.project.voicetotask.ui.theme.VoiceToTaskTheme
import kotlinx.coroutines.flow.MutableStateFlow
import java.io.File

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  private val reminderNavigation = MutableStateFlow<ReminderNavigation?>(null)

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    reminderNavigation.value = intent.toReminderNavigation()
    setContent {
      val reminderDestination by reminderNavigation.collectAsState()
      VoiceToTaskTheme {
        VoiceToTaskAppNav(
          initialTaskId = reminderDestination?.taskId,
          reminderNavigationEvent = reminderDestination?.eventId
        )
      }
    }
  }

  override fun onNewIntent(intent: Intent) {
    super.onNewIntent(intent)
    setIntent(intent)
    reminderNavigation.value = intent.toReminderNavigation()
  }

  private fun Intent.toReminderNavigation(): ReminderNavigation? {
    val taskId = getStringExtra(TaskReminderContract.EXTRA_TASK_ID)
      ?.takeIf { it.isNotBlank() }
      ?: return null
    return ReminderNavigation(taskId = taskId, eventId = System.nanoTime())
  }
}

private data class ReminderNavigation(
    val taskId: String,
    val eventId: Long
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTaskAppNav(
    initialTaskId: String? = null,
    reminderNavigationEvent: Long? = null
) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var showRecordSheet by remember { mutableStateOf(false) }

    LaunchedEffect(initialTaskId, reminderNavigationEvent) {
        if (!initialTaskId.isNullOrBlank()) {
            navController.navigate(Screen.TaskDetails.createRoute(initialTaskId)) {
                launchSingleTop = true
            }
        }
    }

    Scaffold(
        bottomBar = {
            // Only show bottom bar on top-level screens
            val showBottomBar = currentRoute in listOf(
                Screen.Home.route,
                Screen.MeetingHistory.route,
                Screen.Settings.route
            )
            if (showBottomBar) {
                BottomNavigationBar(
                    currentRoute = currentRoute,
                    onNavigateToRoute = { route ->
                        if (currentRoute != route) {
                            navController.navigate(route) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Screen.Home.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Home.route) {
                val viewModel: HomeViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                
                HomeScreen(
                    uiState = uiState,
                    onSearchQueryChange = viewModel::onSearchQueryChange,
                    onCategorySelected = viewModel::onCategorySelected,
                    onTaskClick = { taskId -> 
                        navController.navigate(Screen.TaskDetails.createRoute(taskId))
                    },
                    onTaskCheckedChange = viewModel::toggleTaskCompletion,
                    onTaskDelete = viewModel::deleteTask,
                    onAvatarClick = { /* Handle avatar click */ },
                    onStatsTaskClick = { /* Handle stats click */ },
                    onStatsMeetingClick = { 
                        navController.navigate(Screen.MeetingHistory.route)
                    },
                    onAddTaskClick = {
                        navController.navigate(Screen.TaskDetails.createRoute(NEW_TASK_ID))
                    },
                    onRecordClick = { showRecordSheet = true },
                    onExportTasksClick = {
                        shareTextFile(
                            context = context,
                            chooserTitle = "Export visible tasks",
                            fileName = "tasks_export_${ExportFormatter.timestampForFile()}.csv",
                            mimeType = "text/csv",
                            text = ExportFormatter.visibleTasksCsv(uiState.recentTasks)
                        )
                    }
                )

                // Trigger record sheet (e.g., from a FAB or specific action)
                if (showRecordSheet) {
                    val recordViewModel: RecordViewModel = hiltViewModel()
                    val recordUiState by recordViewModel.uiState.collectAsState()
                    val amplitude by recordViewModel.amplitudeFlow.collectAsState()
                    val audioPickerLauncher = rememberLauncherForActivityResult(
                        contract = ActivityResultContracts.GetContent()
                    ) { uri ->
                        uri?.let { recordViewModel.processUploadedAudio(context, it) }
                    }

                    RecordBottomSheet(
                        uiState = recordUiState,
                        amplitude = amplitude,
                        onStartRecording = { recordViewModel.startRecording(context) },
                        onStopRecording = { recordViewModel.stopRecordingAndProcess() },
                        onPauseResume = recordViewModel::togglePauseRecording,
                        onCancelRecording = recordViewModel::cancelRecording,
                        onRetryProcessing = recordViewModel::retryProcessing,
                        onDeleteCurrentAudio = recordViewModel::deleteCurrentAudio,
                        onPromptProfileSelected = recordViewModel::onPromptProfileSelected,
                        onUploadClick = { audioPickerLauncher.launch("audio/*") },
                        onDismissRequest = { showRecordSheet = false }
                    )

                    LaunchedEffect(recordUiState.recentMeetingId) {
                        recordUiState.recentMeetingId?.let { meetingId ->
                            recordViewModel.consumeRecentMeeting()
                            showRecordSheet = false
                            navController.navigate(Screen.AIResult.createRoute(meetingId))
                        }
                    }
                }
            }

            composable(Screen.MeetingHistory.route) {
                val viewModel: HistoryViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                HistoryScreen(
                    uiState = uiState,
                    onSearchChange = viewModel::onSearchQueryChange,
                    onFilterChange = viewModel::onFilterChange,
                    onMeetingClick = { meeting ->
                        navController.navigate(Screen.MeetingDetails.createRoute(meeting.id))
                    },
                    onExportBackupClick = {
                        shareTextFile(
                            context = context,
                            chooserTitle = "Export local backup",
                            fileName = "voice_to_task_backup_${ExportFormatter.timestampForFile()}.json",
                            mimeType = "application/json",
                            text = viewModel.buildBackupJson()
                        )
                    }
                )
            }

            composable(
                route = Screen.MeetingDetails.route,
                arguments = listOf(navArgument("meetingId") { type = NavType.StringType })
            ) {
                val viewModel: MeetingDetailViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                MeetingDetailScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onShareClick = {
                        shareText(
                            context = context,
                            title = "Share meeting notes",
                            text = viewModel.buildMeetingNotesText()
                        )
                    },
                    onShareTranscript = {
                        shareText(
                            context = context,
                            title = "Share transcript",
                            text = viewModel.buildTranscriptText()
                        )
                    },
                    onShareMeetingNotes = {
                        shareText(
                            context = context,
                            title = "Share meeting notes",
                            text = viewModel.buildMeetingNotesText()
                        )
                    },
                    onPlayPauseClick = { 
                        if (uiState.playerState.isPlaying) viewModel.pauseAudio()
                        else if (uiState.playerState.currentPosition > 0) viewModel.resumeAudio()
                        else viewModel.playAudio()
                    },
                    onSeek = viewModel::seekTo,
                    onPlaybackSpeedChange = viewModel::setPlaybackSpeed,
                    onCopyTranscript = {
                        copyText(
                            context = context,
                            label = "Transcript",
                            text = viewModel.buildTranscriptText(),
                            confirmation = "Transcript copied."
                        )
                    },
                    onCopySummary = {
                        copyText(
                            context = context,
                            label = "Summary",
                            text = viewModel.buildSummaryText(),
                            confirmation = "Summary copied."
                        )
                    },
                    onCopyMeetingNotes = {
                        copyText(
                            context = context,
                            label = "Meeting notes",
                            text = viewModel.buildMeetingNotesText(),
                            confirmation = "Meeting notes copied."
                        )
                    },
                    onExportTranscript = {
                        shareTextFile(
                            context = context,
                            chooserTitle = "Export transcript",
                            fileName = "meeting_transcript_${ExportFormatter.timestampForFile()}.txt",
                            mimeType = "text/plain",
                            text = ExportFormatter.meetingTranscriptText(uiState)
                        )
                    },
                    onExportMeetingNotes = {
                        shareTextFile(
                            context = context,
                            chooserTitle = "Export meeting notes",
                            fileName = "meeting_notes_${ExportFormatter.timestampForFile()}.txt",
                            mimeType = "text/plain",
                            text = ExportFormatter.meetingNotesText(uiState)
                        )
                    },
                    onExportLinkedTasks = {
                        shareTextFile(
                            context = context,
                            chooserTitle = "Export linked tasks",
                            fileName = "meeting_tasks_${ExportFormatter.timestampForFile()}.csv",
                            mimeType = "text/csv",
                            text = ExportFormatter.linkedTasksCsv(uiState.tasks)
                        )
                    },
                    onTranscriptSearchChange = viewModel::onTranscriptSearchQueryChange,
                    onTaskCheckedChange = viewModel::toggleTaskCompletion,
                    onAddTaskClick = { /* Handle add task */ },
                    onTaskClick = { taskId ->
                        navController.navigate(Screen.TaskDetails.createRoute(taskId))
                    }
                )
            }

            composable(
                route = Screen.TaskDetails.route,
                arguments = listOf(navArgument("taskId") { type = NavType.StringType })
            ) {
                val viewModel: TaskDetailViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()
                val notificationPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { granted ->
                    if (granted) {
                        viewModel.save()
                    } else {
                        Toast.makeText(
                            context,
                            "Task saved, but notifications are disabled.",
                            Toast.LENGTH_LONG
                        ).show()
                        viewModel.save()
                    }
                }

                LaunchedEffect(uiState.isFinished) {
                    if (uiState.isFinished) {
                        navController.popBackStack()
                    }
                }

                TaskDetailScreen(
                    uiState = uiState,
                    onTitleChange = viewModel::onTitleChange,
                    onNotesChange = viewModel::onNotesChange,
                    onCategoryChange = viewModel::onCategoryChange,
                    onPriorityChange = viewModel::onPriorityChange,
                    onAssigneeChange = viewModel::onAssigneeChange,
                    onDueAtChange = viewModel::onDueAtChange,
                    onCompletedChange = viewModel::onCompletedChange,
                    onReminderChange = viewModel::onReminderChange,
                    onSaveClick = {
                        val needsPermission =
                            uiState.reminderTime != null &&
                            Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
                            ContextCompat.checkSelfPermission(
                                context,
                                Manifest.permission.POST_NOTIFICATIONS
                            ) != PackageManager.PERMISSION_GRANTED

                        if (needsPermission) {
                            notificationPermissionLauncher.launch(
                                Manifest.permission.POST_NOTIFICATIONS
                            )
                        } else {
                            viewModel.save()
                        }
                    },
                    onDeleteClick = viewModel::delete,
                    onBackClick = { navController.popBackStack() },
                    showDeleteButton = uiState.isEditMode
                )
            }

            composable(
                route = Screen.AIResult.route,
                arguments = listOf(navArgument("meetingId") { type = NavType.StringType })
            ) {
                val viewModel: AiResultViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                LaunchedEffect(uiState.isFinished) {
                    if (uiState.isFinished) {
                        navController.popBackStack(Screen.Home.route, inclusive = false)
                    }
                }

                AiResultScreen(
                    uiState = uiState,
                    onBackClick = viewModel::requestBack,
                    onSaveTasksClick = viewModel::saveChanges,
                    onDiscardClick = viewModel::discard,
                    onStayClick = viewModel::stayOnResult,
                    onMeetingSummaryChange = viewModel::onMeetingSummaryChange,
                    onDecisionsChange = viewModel::onDecisionsChange,
                    onBlockersChange = viewModel::onBlockersChange,
                    onFollowUpsChange = viewModel::onFollowUpsChange,
                    onAddTaskClick = viewModel::startAddingTask,
                    onTaskDismiss = viewModel::requestTaskDelete,
                    onTaskClick = viewModel::startEditingTask,
                    onTaskCheckedChange = viewModel::onTaskCheckedChange,
                    onCancelTaskDelete = viewModel::cancelTaskDelete,
                    onConfirmTaskDelete = viewModel::confirmTaskDelete,
                    onTaskTitleChange = viewModel::onEditingTaskTitleChange,
                    onTaskDescriptionChange = viewModel::onEditingTaskDescriptionChange,
                    onTaskAssigneeChange = viewModel::onEditingTaskAssigneeChange,
                    onTaskDueAtChange = viewModel::onEditingTaskDueAtChange,
                    onTaskCategoryChange = viewModel::onEditingTaskCategoryChange,
                    onTaskPriorityChange = viewModel::onEditingTaskPriorityChange,
                    onSaveTaskEdit = viewModel::saveEditingTask,
                    onCancelTaskEdit = viewModel::cancelTaskEditing
                )
            }

            composable(Screen.Settings.route) {
                val viewModel: SettingsViewModel = hiltViewModel()
                val uiState by viewModel.uiState.collectAsState()

                SettingsScreen(
                    uiState = uiState,
                    onEditProfileClick = { /* Handle edit profile */ },
                    onDarkModeToggle = viewModel::onDarkModeToggle,
                    onCloudSyncToggle = viewModel::onCloudSyncToggle,
                    onSyncNowClick = viewModel::onSyncNow,
                    onLanguageClick = viewModel::onLanguageClick,
                    onSignOutClick = viewModel::onSignOut
                )
            }
            
            // Add other screens as needed (TaskDetails, MeetingDetails, etc.)
        }
    }
}

private fun copyText(
    context: Context,
    label: String,
    text: String,
    confirmation: String
) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    clipboard.setPrimaryClip(ClipData.newPlainText(label, text))
    Toast.makeText(context, confirmation, Toast.LENGTH_SHORT).show()
}

private fun shareText(
    context: Context,
    title: String,
    text: String
) {
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = "text/plain"
        putExtra(Intent.EXTRA_TEXT, text)
    }
    context.startActivity(Intent.createChooser(sendIntent, title))
}

private fun shareTextFile(
    context: Context,
    chooserTitle: String,
    fileName: String,
    mimeType: String,
    text: String
) {
    val exportDir = File(context.cacheDir, "exports").apply { mkdirs() }
    val exportFile = File(exportDir, fileName)
    exportFile.writeText(text, Charsets.UTF_8)
    val uri = FileProvider.getUriForFile(
        context,
        "${context.packageName}.fileprovider",
        exportFile
    )
    val sendIntent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        putExtra(Intent.EXTRA_TEXT, text)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(Intent.createChooser(sendIntent, chooserTitle))
}
