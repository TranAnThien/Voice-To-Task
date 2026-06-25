package com.project.voicetotask.domain.model

data class PlayerState(
    val isPlaying: Boolean = false,
    val currentPosition: Long = 0L,
    val totalDuration: Long = 0L
)
