package com.project.voicetotask.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Fts4

@Fts4(contentEntity = MeetingEntity::class)
@Entity(tableName = "meetings_fts")
data class MeetingFtsEntity(
    @ColumnInfo(name = "transcript")
    val transcript: String
)
