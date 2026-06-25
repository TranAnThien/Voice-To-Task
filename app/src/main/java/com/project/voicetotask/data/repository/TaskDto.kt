package com.project.voicetotask.data.repository

import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TaskDto(
    val title: String?,
    val description: String?,
    val category: String?,
    val priority: String?,
    val isCompleted: Boolean = false
)
