package com.project.voicetotask.domain.usecase

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
    suspend operator fun invoke(audioFile: File): Resource<Pair<Meeting, List<Task>>> {
        when (val transcribeResult = aiRepository.transcribeAudio(audioFile)) {
            is Resource.Error -> return Resource.Error(transcribeResult.message, transcribeResult.cause)
            Resource.Loading -> return Resource.Loading
            is Resource.Success -> {
                val transcript = transcribeResult.data
                when (val extractResult = aiRepository.extractTasks(transcript)) {
                    is Resource.Error -> return Resource.Error(extractResult.message, extractResult.cause)
                    Resource.Loading -> return Resource.Loading
                    is Resource.Success -> {
                        val tasks = extractResult.data
                        val meetingId = UUID.randomUUID().toString()
                        val meeting = Meeting(
                            id = meetingId,
                            title = "New Meeting", // We might want to generate a title, but this is fine for now
                            date = System.currentTimeMillis(),
                            duration = 0L, // Need real duration if possible, mock for now
                            transcript = transcript,
                            audioFilePath = audioFile.absolutePath
                        )
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
}
