package com.project.voicetotask.domain.repository

import com.project.voicetotask.domain.model.PlayerState
import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioPlayer {
    fun playFile(file: File)
    fun pause()
    fun resume()
    fun stop()
    fun seekTo(position: Long)
    fun setPlaybackSpeed(speed: Float)
    fun getPlayerStateFlow(): Flow<PlayerState>
}
