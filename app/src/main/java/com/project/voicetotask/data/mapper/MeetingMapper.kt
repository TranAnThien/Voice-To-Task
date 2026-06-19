package com.project.voicetotask.data.mapper

import com.project.voicetotask.data.source.local.entity.MeetingEntity
import com.project.voicetotask.domain.model.Meeting

fun MeetingEntity.toDomain(): Meeting {
    return Meeting(
        id = id,
        title = title,
        date = date,
        duration = duration,
        transcript = transcript,
        audioFilePath = audioFilePath
    )
}

fun Meeting.toEntity(): MeetingEntity {
    return MeetingEntity(
        id = id,
        title = title,
        date = date,
        duration = duration,
        transcript = transcript,
        audioFilePath = audioFilePath
    )
}
