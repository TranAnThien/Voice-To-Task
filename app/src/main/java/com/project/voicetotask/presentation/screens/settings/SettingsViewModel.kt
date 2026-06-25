package com.project.voicetotask.presentation.screens.settings

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import javax.inject.Inject

@HiltViewModel
class SettingsViewModel @Inject constructor() : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onDarkModeToggle(enabled: Boolean) {
        _uiState.update { it.copy(darkModeEnabled = enabled) }
    }

    fun onCloudSyncToggle(enabled: Boolean) {
        _uiState.update { it.copy(cloudSyncEnabled = enabled) }
    }

    fun onSyncNow() {
        // Handle sync now
    }

    fun onLanguageClick() {
        // Handle language selection
    }

    fun onSignOut() {
        // Handle sign out
    }
}
