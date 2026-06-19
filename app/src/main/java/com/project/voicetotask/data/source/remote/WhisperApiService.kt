package com.project.voicetotask.data.source.remote

import com.squareup.moshi.JsonClass
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

interface WhisperApiService {
    @Multipart
    @POST("v1/audio/transcriptions")
    suspend fun transcribeAudio(
        @Header("Authorization") authHeader: String,
        @Part file: MultipartBody.Part,
        @Part model: MultipartBody.Part
    ): Response<WhisperResponseDto>
}

@JsonClass(generateAdapter = true)
data class WhisperResponseDto(
    val text: String
)
