package com.project.voicetotask.domain.usecase

import com.project.voicetotask.domain.model.AiPromptProfile
import com.project.voicetotask.domain.model.Meeting
import com.project.voicetotask.domain.model.Resource
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.AiRepository
import com.project.voicetotask.domain.repository.MeetingRepository
import com.project.voicetotask.domain.repository.TaskRepository
import java.io.File
import java.util.UUID
import javax.inject.Inject

class ProcessMeetingUseCase @Inject constructor(
    private val aiRepository: AiRepository,
    private val meetingRepository: MeetingRepository,
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(
        audioFile: File,
        durationMillis: Long,
        profile: AiPromptProfile = AiPromptProfile.default
    ): Resource<Pair<Meeting, List<Task>>> {
        when (val transcribeResult = aiRepository.transcribeAudio(audioFile)) {
            is Resource.Error -> return Resource.Error(transcribeResult.message, transcribeResult.cause)
            Resource.Loading -> return Resource.Loading
            is Resource.Success -> {
                val rawTranscript = transcribeResult.data
                val meetingId = UUID.randomUUID().toString()
                val baseMeeting = Meeting(
                    id = meetingId,
                    title = "New Meeting",
                    date = System.currentTimeMillis(),
                    duration = durationMillis.coerceAtLeast(0L),
                    transcript = rawTranscript,
                    audioFilePath = audioFile.absolutePath,
                    summary = "",
                    decisionsText = "",
                    blockersText = "",
                    followUpsText = "",
                    isConfirmed = false
                )

                when (val analysisResult = aiRepository.analyzeTranscript(rawTranscript, profile)) {
                    is Resource.Error -> {
                        meetingRepository.insertMeeting(baseMeeting)
                        return Resource.Success(Pair(baseMeeting, emptyList()))
                    }
                    Resource.Loading -> return Resource.Loading
                    is Resource.Success -> {
                        val analysis = analysisResult.data
                        val meeting = baseMeeting.copy(
                            title = analysis.suggestedMeetingTitle ?: baseMeeting.title,
                            transcript = analysis.correctedTranscript,
                            summary = analysis.summary,
                            decisionsText = analysis.keyDecisions.toMultilineText(),
                            blockersText = analysis.blockers.toMultilineText(),
                            followUpsText = analysis.followUps.toMultilineText()
                        )
                        val tasks = analysis.tasks
                        meetingRepository.insertMeeting(meeting)

                        val updatedTasks = tasks.map { 
                            val taskWithMeetingId = it.copy(meetingId = meetingId, id = UUID.randomUUID().toString())
                            taskRepository.insertTask(taskWithMeetingId)
                            taskWithMeetingId
                        }
                        return Resource.Success(Pair(meeting, updatedTasks))
                    }
                }
            }
        }
    }

    private fun List<String>.toMultilineText(): String {
        return joinToString(separator = "\n")
    }
}
