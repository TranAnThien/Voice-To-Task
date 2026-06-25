package com.project.voicetotask.data.repository

import com.project.voicetotask.BuildConfig
import com.project.voicetotask.data.source.remote.CandidateDto
import com.project.voicetotask.data.source.remote.ContentDto
import com.project.voicetotask.data.source.remote.GeminiApiService
import com.project.voicetotask.data.source.remote.GeminiRequestDto
import com.project.voicetotask.data.source.remote.GenerationConfigDto
import com.project.voicetotask.data.source.remote.PartDto
import com.project.voicetotask.data.source.remote.WhisperApiService
import com.project.voicetotask.domain.model.Resource
import com.project.voicetotask.domain.model.Task
import com.project.voicetotask.domain.repository.AiRepository
import com.squareup.moshi.Moshi
import com.squareup.moshi.Types
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File
import java.io.IOException
import java.net.SocketTimeoutException
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

    override suspend fun extractTasks(transcript: String): Resource<List<Task>> {
        return try {
            if (transcript.isBlank()) {
                return Resource.Success(emptyList())
            }

            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isMissingSecret()) {
                return Resource.Error("GEMINI_API_KEY is missing. Check the root .env file.")
            }

            val prompt = """
                Extract actionable tasks from this meeting transcript.
                Return only a valid JSON array. Do not include markdown, explanation, or extra text.
                Each item must have exactly these fields:
                - "title": short task title
                - "description": one sentence with useful context
                - "category": one of "Work", "Personal", "Study", "Meeting", "Other"
                - "priority": one of "High", "Medium", "Low"
                - "isCompleted": false
                If there are no clear tasks, return [].
                
                Transcript:
                $transcript
            """.trimIndent()

            val requestDto = GeminiRequestDto(
                contents = listOf(
                    ContentDto(
                        parts = listOf(PartDto(text = prompt))
                    )
                ),
                generationConfig = GenerationConfigDto(responseMimeType = "application/json")
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
                    return Resource.Success(emptyList())
                }

                val taskListType = Types.newParameterizedType(List::class.java, TaskDto::class.java)
                val adapter = moshi.adapter<List<TaskDto>>(taskListType)
                val dtos = adapter.fromJson(cleanJsonResponse(jsonResponse)) ?: emptyList()

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
                            meetingId = null
                        )
                    }
                }

                Resource.Success(tasks)
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
        return when (category?.trim()) {
            "Work", "Personal", "Study", "Meeting", "Other" -> category.trim()
            else -> "Other"
        }
    }

    private fun normalizePriority(priority: String?): String {
        return when (priority?.trim()) {
            "High", "Medium", "Low" -> priority.trim()
            else -> "Medium"
        }
    }

    private companion object {
        const val MIN_AUDIO_BYTES = 1024L
    }
}
