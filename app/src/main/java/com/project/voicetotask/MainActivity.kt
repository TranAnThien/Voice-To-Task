package com.project.voicetotask

import android.os.Bundle
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
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.project.voicetotask.presentation.components.BottomNavigationBar
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

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    setContent {
      VoiceToTaskTheme {
        VoiceToTaskAppNav()
      }
    }
  }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VoiceToTaskAppNav() {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val context = LocalContext.current
    var showRecordSheet by remember { mutableStateOf(false) }

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
                    onAvatarClick = { /* Handle avatar click */ },
                    onStatsTaskClick = { /* Handle stats click */ },
                    onStatsMeetingClick = { 
                        navController.navigate(Screen.MeetingHistory.route)
                    },
                    onAddTaskClick = {
                        navController.navigate(Screen.TaskDetails.createRoute(NEW_TASK_ID))
                    },
                    onRecordClick = { showRecordSheet = true }
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
                        onUploadClick = { audioPickerLauncher.launch("audio/*") },
                        onDismissRequest = { showRecordSheet = false }
                    )

                    // Navigation logic when processing is done
                    recordUiState.recentMeetingId?.let { meetingId ->
                        showRecordSheet = false
                        navController.navigate(Screen.AIResult.createRoute(meetingId))
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
                    onShareClick = { /* Handle share */ },
                    onPlayPauseClick = { 
                        if (uiState.playerState.isPlaying) viewModel.pauseAudio()
                        else if (uiState.playerState.currentPosition > 0) viewModel.resumeAudio()
                        else viewModel.playAudio()
                    },
                    onSeek = viewModel::seekTo,
                    onCopyTranscript = { /* Handle copy */ },
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
                    onCompletedChange = viewModel::onCompletedChange,
                    onSaveClick = viewModel::save,
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

                AiResultScreen(
                    uiState = uiState,
                    onBackClick = { navController.popBackStack() },
                    onSaveTasksClick = { 
                        viewModel.saveChanges()
                        navController.navigate(Screen.Home.route) {
                            popUpTo(Screen.Home.route) { inclusive = true }
                        }
                    },
                    onTaskDismiss = viewModel::onTaskDismissed,
                    onTaskClick = { task ->
                        navController.navigate(Screen.TaskDetails.createRoute(task.id))
                    },
                    onTaskCheckedChange = viewModel::onTaskCheckedChange
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
