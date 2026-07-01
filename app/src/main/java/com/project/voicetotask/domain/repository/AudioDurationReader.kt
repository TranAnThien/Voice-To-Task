package com.project.voicetotask.domain.repository

import java.io.File

interface AudioDurationReader {
    fun getDurationMillis(audioFile: File): Long?
}
