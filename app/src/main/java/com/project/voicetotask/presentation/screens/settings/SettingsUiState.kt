package com.project.voicetotask.presentation.screens.settings

data class SettingsUiState(
    val displayName: String = "Thien",
    val email: String = "anthien125@gmail.com",
    val cloudSyncEnabled: Boolean = true,
    val darkModeEnabled: Boolean = false,
    val recognitionLanguage: String = "Vietnamese",
    val lastSyncedTime: String = "Synced 5 mins ago"
)
