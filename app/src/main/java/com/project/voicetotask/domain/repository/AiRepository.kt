package com.project.voicetotask.domain.repository

import com.project.voicetotask.domain.model.Resource
import com.project.voicetotask.domain.model.Task
import java.io.File

interface AiRepository {
    suspend fun transcribeAudio(audioFile: File): Resource<String>
    suspend fun extractTasks(transcript: String): Resource<List<Task>>
}
