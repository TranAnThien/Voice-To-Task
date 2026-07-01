package com.project.voicetotask.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "meetings",
    indices = [Index(value = ["id"], unique = true)]
)
data class MeetingEntity(
    @PrimaryKey(autoGenerate = true)
    val rowid: Int = 0,
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "date")
    val date: Long,
    @ColumnInfo(name = "duration")
    val duration: Long,
    @ColumnInfo(name = "transcript")
    val transcript: String,
    @ColumnInfo(name = "audioFilePath")
    val audioFilePath: String?,
    @ColumnInfo(name = "summary", defaultValue = "''")
    val summary: String,
    @ColumnInfo(name = "decisionsText", defaultValue = "''")
    val decisionsText: String,
    @ColumnInfo(name = "blockersText", defaultValue = "''")
    val blockersText: String,
    @ColumnInfo(name = "followUpsText", defaultValue = "''")
    val followUpsText: String,
    @ColumnInfo(name = "isConfirmed", defaultValue = "1")
    val isConfirmed: Boolean
)
