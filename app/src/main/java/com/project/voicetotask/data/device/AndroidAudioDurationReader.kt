package com.project.voicetotask.data.device

import android.media.MediaMetadataRetriever
import com.project.voicetotask.domain.repository.AudioDurationReader
import java.io.File
import javax.inject.Inject

class AndroidAudioDurationReader @Inject constructor() : AudioDurationReader {

    override fun getDurationMillis(audioFile: File): Long? {
        if (!audioFile.exists() || !audioFile.canRead()) return null

        val retriever = MediaMetadataRetriever()
        return try {
            retriever.setDataSource(audioFile.absolutePath)
            retriever.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION)
                ?.toLongOrNull()
                ?.takeIf { it > 0L }
        } catch (_: RuntimeException) {
            null
        } finally {
            retriever.release()
        }
    }
}
