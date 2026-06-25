package com.project.voicetotask.data.device

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.project.voicetotask.domain.model.PlayerState
import com.project.voicetotask.domain.repository.AudioPlayer
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.File
import javax.inject.Inject

class ExoAudioPlayer @Inject constructor(
    @ApplicationContext private val context: Context
) : AudioPlayer {

    private var exoPlayer: ExoPlayer? = null
    private val playerStateFlow = MutableStateFlow(PlayerState())
    private var progressJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.Main)

    private fun initializePlayer() {
        if (exoPlayer == null) {
            exoPlayer = ExoPlayer.Builder(context).build().apply {
                addListener(object : Player.Listener {
                    override fun onIsPlayingChanged(isPlaying: Boolean) {
                        updateState { it.copy(isPlaying = isPlaying) }
                        if (isPlaying) {
                            startProgressTracking()
                        } else {
                            stopProgressTracking()
                        }
                    }

                    override fun onPlaybackStateChanged(playbackState: Int) {
                        if (playbackState == Player.STATE_ENDED) {
                            updateState { it.copy(isPlaying = false, currentPosition = 0L) }
                            stopProgressTracking()
                            seekTo(0L)
                        } else if (playbackState == Player.STATE_READY) {
                            updateState { it.copy(totalDuration = duration.coerceAtLeast(0L)) }
                        }
                    }
                })
            }
        }
    }

    override fun playFile(file: File) {
        try {
            if (!file.exists()) {
                Log.e("ExoAudioPlayer", "File does not exist: ${file.absolutePath}")
                return
            }
            initializePlayer()
            exoPlayer?.apply {
                val mediaItem = MediaItem.fromUri(Uri.fromFile(file))
                setMediaItem(mediaItem)
                prepare()
                play()
            }
        } catch (e: Exception) {
            Log.e("ExoAudioPlayer", "Error playing file: ${e.message}")
        }
    }

    override fun pause() {
        try {
            exoPlayer?.pause()
        } catch (e: Exception) {
            Log.e("ExoAudioPlayer", "Error pausing player: ${e.message}")
        }
    }

    override fun resume() {
        try {
            exoPlayer?.let {
                if (it.playbackState == Player.STATE_IDLE || it.playbackState == Player.STATE_ENDED) {
                    it.prepare()
                }
                it.play()
            }
        } catch (e: Exception) {
            Log.e("ExoAudioPlayer", "Error resuming player: ${e.message}")
        }
    }

    override fun stop() {
        try {
            stopProgressTracking()
            exoPlayer?.apply {
                stop()
                clearMediaItems()
            }
            updateState { PlayerState() }
        } catch (e: Exception) {
            Log.e("ExoAudioPlayer", "Error stopping player: ${e.message}")
        }
    }

    override fun seekTo(position: Long) {
        try {
            exoPlayer?.seekTo(position)
            updateState { it.copy(currentPosition = position) }
        } catch (e: Exception) {
            Log.e("ExoAudioPlayer", "Error seeking: ${e.message}")
        }
    }

    override fun getPlayerStateFlow(): Flow<PlayerState> = playerStateFlow

    private fun startProgressTracking() {
        progressJob?.cancel()
        progressJob = scope.launch {
            while (isActive) {
                exoPlayer?.let {
                    updateState { state -> state.copy(currentPosition = it.currentPosition.coerceAtLeast(0L)) }
                }
                delay(500L)
            }
        }
    }

    private fun stopProgressTracking() {
        progressJob?.cancel()
        progressJob = null
    }

    private inline fun updateState(update: (PlayerState) -> PlayerState) {
        playerStateFlow.value = update(playerStateFlow.value)
    }

    fun release() {
        stopProgressTracking()
        exoPlayer?.release()
        exoPlayer = null
    }
}
