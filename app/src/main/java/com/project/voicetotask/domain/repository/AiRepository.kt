package com.project.voicetotask.domain.repository

import com.project.voicetotask.domain.model.MeetingAnalysis
import com.project.voicetotask.domain.model.Resource
import com.project.voicetotask.domain.model.AiPromptProfile
import java.io.File

interface AiRepository {
    suspend fun transcribeAudio(audioFile: File): Resource<String>
    suspend fun analyzeTranscript(
        transcript: String,
        profile: AiPromptProfile = AiPromptProfile.default
    ): Resource<MeetingAnalysis>
}
