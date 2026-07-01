package com.project.voicetotask.data.source.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = MeetingEntity::class,
            parentColumns = ["id"],
            childColumns = ["meetingId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index("meetingId")]
)
data class TaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,
    @ColumnInfo(name = "title")
    val title: String,
    @ColumnInfo(name = "description")
    val description: String,
    @ColumnInfo(name = "category")
    val category: String,
    @ColumnInfo(name = "priority")
    val priority: String,
    @ColumnInfo(name = "isCompleted")
    val isCompleted: Boolean,
    @ColumnInfo(name = "reminderTime")
    val reminderTime: Long?,
    @ColumnInfo(name = "meetingId")
    val meetingId: String?,
    @ColumnInfo(name = "assigneeName", defaultValue = "'Me'")
    val assigneeName: String,
    @ColumnInfo(name = "dueAt")
    val dueAt: Long?
)
