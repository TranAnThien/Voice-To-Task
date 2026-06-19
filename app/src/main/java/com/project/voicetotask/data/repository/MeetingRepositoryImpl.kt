package com.project.voicetotask.data.repository

import com.project.voicetotask.data.mapper.toDomain
import com.project.voicetotask.data.mapper.toEntity
import com.project.voicetotask.data.source.local.dao.MeetingDao
import com.project.voicetotask.domain.model.Meeting
import com.project.voicetotask.domain.repository.MeetingRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import javax.inject.Inject

class MeetingRepositoryImpl @Inject constructor(
    private val meetingDao: MeetingDao
) : MeetingRepository {

    override fun getAllMeetings(): Flow<List<Meeting>> {
        return meetingDao.getAllMeetings().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getMeetingById(id: String): Flow<Meeting?> {
        return meetingDao.getMeetingById(id).map { it?.toDomain() }
    }

    override fun searchMeetings(query: String): Flow<List<Meeting>> {
        // Prepare query for FTS MATCH. SQLite FTS supports suffix wildcard.
        val ftsQuery = "$query*"
        return meetingDao.searchMeetings(ftsQuery).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun insertMeeting(meeting: Meeting) {
        meetingDao.insertMeeting(meeting.toEntity())
    }

    override suspend fun updateMeeting(meeting: Meeting) {
        meetingDao.updateMeetingFields(
            id = meeting.id,
            title = meeting.title,
            date = meeting.date,
            duration = meeting.duration,
            transcript = meeting.transcript,
            audioFilePath = meeting.audioFilePath
        )
    }

    override suspend fun deleteMeeting(meeting: Meeting) {
        meetingDao.deleteMeetingById(meeting.id)
    }
}
