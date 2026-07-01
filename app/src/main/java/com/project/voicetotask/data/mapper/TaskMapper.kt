package com.project.voicetotask.data.mapper

import com.project.voicetotask.data.source.local.entity.TaskEntity
import com.project.voicetotask.domain.model.Task

fun TaskEntity.toDomain(): Task {
    return Task(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = priority,
        isCompleted = isCompleted,
        reminderTime = reminderTime,
        meetingId = meetingId,
        assigneeName = assigneeName,
        dueAt = dueAt
    )
}

fun Task.toEntity(): TaskEntity {
    return TaskEntity(
        id = id,
        title = title,
        description = description,
        category = category,
        priority = priority,
        isCompleted = isCompleted,
        reminderTime = reminderTime,
        meetingId = meetingId,
        assigneeName = assigneeName,
        dueAt = dueAt
    )
}
