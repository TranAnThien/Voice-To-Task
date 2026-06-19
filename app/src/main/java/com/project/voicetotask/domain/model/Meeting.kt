package com.project.voicetotask.domain.model

data class Meeting(
    val id: String,
    val title: String,
    val date: Long,
    val duration: Long,
    val transcript: String,
    val audioFilePath: String?
)
