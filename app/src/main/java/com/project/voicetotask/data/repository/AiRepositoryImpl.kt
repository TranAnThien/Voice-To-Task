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
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File
import javax.inject.Inject

class AiRepositoryImpl @Inject constructor(
    private val whisperApiService: WhisperApiService,
    private val geminiApiService: GeminiApiService,
    private val moshi: Moshi
) : AiRepository {

    override suspend fun transcribeAudio(audioFile: File): Resource<String> {
        return try {
            val requestFile = audioFile.asRequestBody("audio/*".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)
            val modelPart = MultipartBody.Part.createFormData("model", "whisper-1")

            val apiKey = BuildConfig.OPENAI_API_KEY
            if (apiKey.isNullOrEmpty()) {
                return Resource.Error("OpenAI API Key is missing. Check .env")
            }

            val response = whisperApiService.transcribeAudio("Bearer $apiKey", filePart, modelPart)
            if (response.isSuccessful) {
                response.body()?.text?.let {
                    Resource.Success(it)
                } ?: Resource.Error("Empty response body from Whisper API")
            } else {
                Resource.Error("Whisper API error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to transcribe audio", e)
        }
    }

    override suspend fun extractTasks(transcript: String): Resource<List<Task>> {
        return try {
            val apiKey = BuildConfig.GEMINI_API_KEY
            if (apiKey.isNullOrEmpty()) {
                return Resource.Error("Gemini API Key is missing. Check .env")
            }

            val prompt = """
                Extract tasks from the following meeting transcript. 
                Return the result strictly as a JSON array of objects. Do not use markdown blocks like ```json ... ```, just pure JSON array.
                Each object must have:
                "title" (string)
                "description" (string)
                "category" (string, one of: Work, Personal, Study, Other)
                "priority" (string, one of: High, Medium, Low)
                "isCompleted" (boolean, false)
                
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
                val jsonResponse = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text
                if (jsonResponse != null) {
                    val taskListType = Types.newParameterizedType(List::class.java, TaskDto::class.java)
                    val adapter = moshi.adapter<List<TaskDto>>(taskListType)

                    // Sometimes Gemini returns with markdown even when responseMimeType is json, so let's clean it just in case
                    val cleanJson = jsonResponse.removePrefix("```json").removeSuffix("```").trim()
                    
                    val dtos = adapter.fromJson(cleanJson) ?: emptyList()
                    val tasks = dtos.map {
                        Task(
                            id = java.util.UUID.randomUUID().toString(),
                            title = it.title,
                            description = it.description,
                            category = it.category,
                            priority = it.priority,
                            isCompleted = it.isCompleted,
                            reminderTime = null,
                            meetingId = null
                        )
                    }
                    Resource.Success(tasks)
                } else {
                    Resource.Error("Empty response body from Gemini API")
                }
            } else {
                Resource.Error("Gemini API error: ${response.code()} ${response.message()}")
            }
        } catch (e: Exception) {
            Resource.Error("Failed to extract tasks", e)
        }
    }
}
