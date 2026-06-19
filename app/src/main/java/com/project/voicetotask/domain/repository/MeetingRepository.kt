package com.project.voicetotask.domain.repository

import com.project.voicetotask.domain.model.Meeting
import kotlinx.coroutines.flow.Flow

interface MeetingRepository {
    fun getAllMeetings(): Flow<List<Meeting>>
    fun getMeetingById(id: String): Flow<Meeting?>
    fun searchMeetings(query: String): Flow<List<Meeting>>
    suspend fun insertMeeting(meeting: Meeting)
    suspend fun updateMeeting(meeting: Meeting)
    suspend fun deleteMeeting(meeting: Meeting)
}
