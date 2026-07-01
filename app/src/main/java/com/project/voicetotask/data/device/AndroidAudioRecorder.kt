package com.project.voicetotask.data.device

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import com.project.voicetotask.domain.repository.AudioRecorder
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import java.io.File
import javax.inject.Inject

class AndroidAudioRecorder @Inject constructor(
    @param:ApplicationContext private val context: Context
) : AudioRecorder {

    private var recorder: MediaRecorder? = null
    private val amplitudeFlow = MutableStateFlow(0)
    private var amplitudeJob: Job? = null
    private val scope = CoroutineScope(Dispatchers.IO)

    private fun createRecorder(): MediaRecorder {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }
    }

    override fun startRecording(outputFile: File): Boolean {
        if (ContextCompat.checkSelfPermission(
                context,
                android.Manifest.permission.RECORD_AUDIO
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("AndroidAudioRecorder", "Cannot start recording: RECORD_AUDIO permission not granted")
            return false
        }

        return try {
            stopRecording() // ensure stopped before starting
            
            recorder = createRecorder().apply {
                setAudioSource(MediaRecorder.AudioSource.MIC)
                setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
                setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
                setOutputFile(outputFile.absolutePath)
                prepare()
                start()
            }
            startAmplitudeTracking()
            true
        } catch (e: SecurityException) {
            Log.e("AndroidAudioRecorder", "Permission denied for recording audio: ${e.message}")
            false
        } catch (e: Exception) {
            Log.e("AndroidAudioRecorder", "Error starting recording: ${e.message}")
            false
        }
    }

    override fun stopRecording() {
        try {
            stopAmplitudeTracking()
            recorder?.apply {
                stop()
                reset()
                release()
            }
            recorder = null
        } catch (e: Exception) {
            Log.e("AndroidAudioRecorder", "Error stopping recording: ${e.message}")
        }
    }

    override fun pauseRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.pause()
                stopAmplitudeTracking()
            } else {
                Log.w("AndroidAudioRecorder", "Pause not supported below API 24")
            }
        } catch (e: Exception) {
            Log.e("AndroidAudioRecorder", "Error pausing recording: ${e.message}")
        }
    }

    override fun resumeRecording() {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                recorder?.resume()
                startAmplitudeTracking()
            } else {
                Log.w("AndroidAudioRecorder", "Resume not supported below API 24")
            }
        } catch (e: Exception) {
            Log.e("AndroidAudioRecorder", "Error resuming recording: ${e.message}")
        }
    }

    override fun getAmplitudeFlow(): Flow<Int> = amplitudeFlow

    private fun startAmplitudeTracking() {
        amplitudeJob?.cancel()
        amplitudeJob = scope.launch {
            while (isActive) {
                val maxAmp = try {
                    recorder?.maxAmplitude ?: 0
                } catch (e: Exception) {
                    0
                }
                amplitudeFlow.value = maxAmp
                delay(100L)
            }
        }
    }

    private fun stopAmplitudeTracking() {
        amplitudeJob?.cancel()
        amplitudeJob = null
        amplitudeFlow.value = 0
    }
}
