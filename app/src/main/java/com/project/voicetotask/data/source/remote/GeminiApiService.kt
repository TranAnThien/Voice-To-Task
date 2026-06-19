package com.project.voicetotask.data.source.remote

import com.squareup.moshi.JsonClass
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST
import retrofit2.http.Query

interface GeminiApiService {
    // We assume using Google AI Studio Gemini API format
    @POST("v1beta/models/gemini-2.5-flash:generateContent")
    suspend fun generateContent(
        @Query("key") apiKey: String,
        @Body request: GeminiRequestDto
    ): Response<GeminiResponseDto>
}

// Request DTOs
@JsonClass(generateAdapter = true)
data class GeminiRequestDto(
    val contents: List<ContentDto>,
    val generationConfig: GenerationConfigDto? = null
)

@JsonClass(generateAdapter = true)
data class ContentDto(
    val parts: List<PartDto>
)

@JsonClass(generateAdapter = true)
data class PartDto(
    val text: String
)

@JsonClass(generateAdapter = true)
data class GenerationConfigDto(
    val responseMimeType: String? = null
)

// Response DTOs
@JsonClass(generateAdapter = true)
data class GeminiResponseDto(
    val candidates: List<CandidateDto>? = null 
)

@JsonClass(generateAdapter = true)
data class CandidateDto(
    val content: ContentDto? = null
)
