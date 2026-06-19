package com.project.voicetotask.data.source.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.project.voicetotask.data.source.local.entity.MeetingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface MeetingDao {
    @Query("SELECT * FROM meetings ORDER BY date DESC")
    fun getAllMeetings(): Flow<List<MeetingEntity>>

    @Query("SELECT * FROM meetings WHERE id = :id")
    fun getMeetingById(id: String): Flow<MeetingEntity?>

    @Query("""
        SELECT meetings.* FROM meetings
        JOIN meetings_fts ON meetings.rowid = meetings_fts.rowid
        WHERE meetings_fts MATCH :query
        ORDER BY date DESC
    """)
    fun searchMeetings(query: String): Flow<List<MeetingEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMeeting(meeting: MeetingEntity)

    @Query("UPDATE meetings SET title = :title, date = :date, duration = :duration, transcript = :transcript, audioFilePath = :audioFilePath WHERE id = :id")
    suspend fun updateMeetingFields(id: String, title: String, date: Long, duration: Long, transcript: String, audioFilePath: String?)

    @Query("DELETE FROM meetings WHERE id = :id")
    suspend fun deleteMeetingById(id: String)

}
