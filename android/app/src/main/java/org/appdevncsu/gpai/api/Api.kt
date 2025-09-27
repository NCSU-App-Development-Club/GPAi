package org.appdevncsu.gpai.api

import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.Question
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

const val APIKEY = "ENTER-KEY-HERE"

interface Api {

    @POST("completions")
    @Headers("Authorization: Bearer $APIKEY", "Content-Type: application/json")
    suspend fun askQuestion(
        @Body question: Question
    ): Response<Answer>

}