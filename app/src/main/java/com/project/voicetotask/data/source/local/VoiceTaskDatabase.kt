package com.project.voicetotask.data.source.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.project.voicetotask.data.source.local.dao.MeetingDao
import com.project.voicetotask.data.source.local.dao.TaskDao
import com.project.voicetotask.data.source.local.entity.MeetingEntity
import com.project.voicetotask.data.source.local.entity.MeetingFtsEntity
import com.project.voicetotask.data.source.local.entity.TaskEntity

@Database(
    entities = [MeetingEntity::class, MeetingFtsEntity::class, TaskEntity::class],
    version = 1,
    exportSchema = false
)
abstract class VoiceTaskDatabase : RoomDatabase() {
    abstract fun meetingDao(): MeetingDao
    abstract fun taskDao(): TaskDao
}
