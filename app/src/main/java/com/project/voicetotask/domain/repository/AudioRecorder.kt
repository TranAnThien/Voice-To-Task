package com.project.voicetotask.domain.repository

import kotlinx.coroutines.flow.Flow
import java.io.File

interface AudioRecorder {
    fun startRecording(outputFile: File): Boolean
    fun stopRecording()
    fun pauseRecording()
    fun resumeRecording()
    fun getAmplitudeFlow(): Flow<Int>
}
