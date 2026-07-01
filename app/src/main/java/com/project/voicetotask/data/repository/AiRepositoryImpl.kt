package com.project.voicetotask.data.repository

import com.project.voicetotask.BuildConfig
import com.project.voicetotask.data.source.remote.ContentDto
import com.project.voicetotask.data.source.remote.GeminiApiService
import com.project.voicetotask.data.source.remote.GeminiRequestDto
import com.project.voicetotask.data.source.remote.GenerationConfigDto
import com.project.voicetotask.data.source.remote.PartDto
import com.project.voicetotask.data.source.remote.WhisperApiService
import com.project.voicetotask.domain.model.DEFAULT_ASSIGNEE
import com.project.voicetotask.domain.model.AiPromptProfile
import com.project.voicetotask.domain.model.MeetingAnalysis
import com.project.voicetotask.domain.model.Resource
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.AiRepository
import com.squareup.moshi.Moshi
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
import java.time.Instant
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.UUID
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val whisperApiService: WhisperApiService,
    private val geminiApiService: GeminiApiService,
    private val moshi: Moshi
) : AiRepository {

    override suspend fun transcribeAudio(audioFile: File): Resource<String> {
        return try {
            if (!audioFile.exists() || audioFile.length() <= 0L) {
                return Resource.Error("Audio file is missing or empty.")
            }
            if (!audioFile.canRead()) {
                return Resource.Error("Audio file cannot be read.")
            }
            if (audioFile.length() < MIN_AUDIO_BYTES) {
                return Resource.Error("Audio file is too short or invalid. Please record a little longer.")
            }

            val apiKey = BuildConfig.OPENAI_API_KEY
            if (apiKey.isMissingSecret()) {
                return Resource.Error("OPENAI_API_KEY is missing. Check the root .env file.")
            }

            val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val modelPart = MultipartBody.Part.createFormData("model", "whisper-large-v3")

            val response = whisperApiService.transcribeAudio("Bearer $apiKey", filePart, modelPart)
            if (response.isSuccessful) {
                val transcript = response.body()?.text?.trim()
                if (transcript.isNullOrEmpty()) {
                    Resource.Error("Transcription response was empty.")
                } else {
                    Resource.Success(transcript)
                }
            } else {
                Resource.Error(httpErrorMessage("Transcription API", response.code()))
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Transcription timed out. Try again with a shorter audio file or a better connection.", e)
        } catch (e: IOException) {
            Resource.Error("Network error while transcribing audio. Check your connection.", e)
        } catch (e: Exception) {
            Resource.Error("Failed to transcribe audio.", e)
        }
    }

    override suspend fun analyzeTranscript(
        transcript: String,
        profile: AiPromptProfile
    ): Resource<MeetingAnalysis> {
        return try {
            if (transcript.isBlank()) {
                return Resource.Success(
                    MeetingAnalysis(
                        correctedTranscript = transcript,
                        suggestedMeetingTitle = null,
                        summary = "",
                        keyDecisions = emptyList(),
                        blockers = emptyList(),
                        followUps = emptyList(),
                        tasks = emptyList()
                    )
                )
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isMissingSecret()) {
                return Resource.Error("GEMINI_API_KEY is missing. Check the root .env file.")
            }

            val now = ZonedDateTime.now()
            val safeProfile = profile
            val prompt = buildAnalysisPrompt(
                transcript = transcript,
                now = now,
                profile = safeProfile
            )

            val requestDto = GeminiRequestDto(
                contents = listOf(
                    ContentDto(
                        parts = listOf(PartDto(text = prompt))
                    )
                ),
                generationConfig = GenerationConfigDto(
                    responseMimeType = "application/json",
                    temperature = 0.2
                )
            )

            val response = geminiApiService.generateContent(apiKey, requestDto)
            if (response.isSuccessful) {
                val jsonResponse = response.body()
                    ?.candidates
                    ?.firstOrNull()
                    ?.content
                    ?.parts
                    ?.firstOrNull()
                    ?.text
                    ?.trim()

                if (jsonResponse.isNullOrEmpty()) {
                    return Resource.Success(
                        MeetingAnalysis(
                            correctedTranscript = transcript,
                            suggestedMeetingTitle = null,
                            summary = "",
                            keyDecisions = emptyList(),
                            blockers = emptyList(),
                            followUps = emptyList(),
                            tasks = emptyList()
                        )
                    )
                }

                val adapter = moshi.adapter(MeetingAnalysisDto::class.java)
                val analysisDto = adapter.fromJson(cleanJsonResponse(jsonResponse))
                    ?: return Resource.Error("Task extraction response was empty.")
                val dtos = analysisDto.tasks.orEmpty()

                val tasks = dtos.mapNotNull {
                    val title = it.title?.trim().orEmpty()
                    if (title.isBlank()) {
                        null
                    } else {
                        Task(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = it.description?.trim().orEmpty(),
                            category = normalizeCategory(it.category),
                            priority = normalizePriority(it.priority),
                            isCompleted = it.isCompleted,
                            reminderTime = null,
                            meetingId = null,
                            assigneeName = it.assigneeName?.trim().orEmpty()
                                .ifBlank { DEFAULT_ASSIGNEE },
                            dueAt = parseDeadlineIso(it.deadlineIso)
                        )
                    }
                }

                Resource.Success(
                    MeetingAnalysis(
                        correctedTranscript = analysisDto.correctedTranscript
                            ?.trim()
                            .orEmpty()
                            .ifBlank { transcript },
                        suggestedMeetingTitle = analysisDto.suggestedMeetingTitle
                            ?.trim()
                            ?.takeIf { it.isNotBlank() },
                        summary = analysisDto.summary.cleanText(),
                        keyDecisions = analysisDto.keyDecisions.cleanItems(),
                        blockers = analysisDto.blockers.cleanItems(),
                        followUps = analysisDto.followUps.cleanItems(),
                        tasks = tasks
                    )
                )
            } else {
                Resource.Error(httpErrorMessage("Task extraction API", response.code()))
            }
        } catch (e: SocketTimeoutException) {
            Resource.Error("Task extraction timed out. Try again later.", e)
        } catch (e: IOException) {
            Resource.Error("Network error while extracting tasks. Check your connection.", e)
        } catch (e: Exception) {
            Resource.Error("Task extraction response was not valid JSON.", e)
        }
    }

    private fun buildAnalysisPrompt(
        transcript: String,
        now: ZonedDateTime,
        profile: AiPromptProfile
    ): String {
        return """
                Analyze this transcript as an audio analysis and task-management assistant.
                Selected analysis profile: ${profile.displayName}
                Profile description: ${profile.description}
                Profile-specific guidance:
                ${profile.promptGuidance}

                Current date/time: ${now.format(DateTimeFormatter.ISO_ZONED_DATE_TIME)}
                Current timezone: ${now.zone.id}

                Return only one valid JSON object. Do not use markdown or add explanation.
                The object must have exactly this structure:
                {
                  "suggestedMeetingTitle": null,
                  "correctedTranscript": "corrected transcript",
                  "summary": "concise meeting summary in 3 to 5 sentences",
                  "keyDecisions": ["decision that was explicitly agreed"],
                  "blockers": ["blocker, risk, dependency, or unresolved issue"],
                  "followUps": ["question or next discussion item that is not a concrete task"],
                  "tasks": [
                    {
                      "title": "short actionable title",
                      "description": "one sentence of useful context",
                      "assigneeName": "person name or Me",
                      "deadlineText": null,
                      "deadlineIso": null,
                      "category": "Work|Personal|Study|Meeting|Other",
                      "priority": "High|Medium|Low",
                      "isCompleted": false
                    }
                  ]
                }

                Correct obvious speech-to-text mistakes only when context is sufficiently clear.
                Preserve the original meaning and do not invent facts or technical terms.
                Normalize well-supported technical vocabulary such as Firebase, Firestore, Room,
                Jetpack Compose, Kotlin, Android Studio, Gradle, KSP, API, UI, backend, database,
                Hilt, and Retrofit.

                Write summary as 3 to 5 concise sentences covering the meeting purpose,
                important context, and outcome. Key decisions must contain only decisions
                that participants clearly agreed or finalized. Blockers must contain risks,
                dependencies, constraints, unresolved problems, or unclear requirements.
                Follow-ups must contain questions or next discussion items that still need
                clarification but are not concrete enough to become tasks.
                Return an empty array for keyDecisions, blockers, followUps, or tasks when
                that category has no supported information.

                Determine task ownership from context. Examples: "An làm", "Minh phụ trách",
                or "giao cho Thiên" mean that named person is the assignee.
                If no person is clearly assigned, use "Me".

                Resolve relative deadlines such as "ngày mai", "thứ sáu", or "trước 8 giờ tối"
                using the supplied current date/time and timezone. Set deadlineIso to null when no
                deadline is stated or the deadline cannot be resolved confidently. Never invent a deadline.
                Return an empty tasks array when there are no clear actionable commitments.

                Transcript:
                $transcript
            """.trimIndent()
    }

    private fun String?.isMissingSecret(): Boolean {
        return isNullOrBlank() || this == "DEFAULT_API_KEY"
    }

    private fun cleanJsonResponse(response: String): String {
        return response
            .trim()
            .removePrefix("```json")
            .removePrefix("```JSON")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()
    }

    private fun String?.cleanText(): String {
        return this?.trim().orEmpty()
    }

    private fun List<String>?.cleanItems(): List<String> {
        return this.orEmpty()
            .map { it.trim().removePrefix("-").trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    private fun httpErrorMessage(provider: String, code: Int): String {
        return when (code) {
            401 -> "$provider rejected the request: invalid or missing API key."
            403 -> "$provider rejected the request: permission denied for this API key."
            429 -> "$provider rate limit reached. Try again later."
            in 500..599 -> "$provider server error. Try again later."
            else -> "$provider request failed with HTTP $code."
        }
    }

    private fun normalizeCategory(category: String?): String {
        return when (category?.trim()?.lowercase()) {
            "work" -> "Work"
            "personal" -> "Personal"
            "study" -> "Study"
            "meeting" -> "Meeting"
            "other" -> "Other"
            else -> "Other"
        }
    }

    private fun normalizePriority(priority: String?): String {
        return when (priority?.trim()?.lowercase()) {
            "high" -> "High"
            "medium" -> "Medium"
            "low" -> "Low"
            else -> "Medium"
        }
    }

    private fun parseDeadlineIso(deadlineIso: String?): Long? {
        val value = deadlineIso?.trim()?.takeIf { it.isNotEmpty() } ?: return null
        return try {
            Instant.parse(value).toEpochMilli()
        } catch (_: DateTimeParseException) {
            try {
                OffsetDateTime.parse(value).toInstant().toEpochMilli()
            } catch (_: DateTimeParseException) {
                try {
                    ZonedDateTime.parse(value).toInstant().toEpochMilli()
                } catch (_: DateTimeParseException) {
                    try {
                        LocalDateTime.parse(value)
                            .atZone(ZoneId.systemDefault())
                            .toInstant()
                            .toEpochMilli()
                    } catch (_: DateTimeParseException) {
                        null
                    }
                }
            }
        }
    }

    private companion object {
        const val MIN_AUDIO_BYTES = 1024L
    }
}
