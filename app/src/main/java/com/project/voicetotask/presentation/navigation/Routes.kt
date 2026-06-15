package com.project.voicetotask.presentation.navigation

sealed class Screen(val route: String) {
    object Onboarding : Screen("onboarding")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    
    object Home : Screen("home")
    object MeetingHistory : Screen("meeting_history")
    object Settings : Screen("settings")
    
    object MeetingDetails : Screen("meeting_details/{meetingId}") {
        fun createRoute(meetingId: String) = "meeting_details/$meetingId"
    }
    object TaskDetails : Screen("task_details/{taskId}") {
        fun createRoute(taskId: String) = "task_details/$taskId"
    }
    object Recording : Screen("recording")
    object AIResult : Screen("ai_result/{meetingId}") {
        fun createRoute(meetingId: String) = "ai_result/$meetingId"
    }
}
