package org.appdevncsu.gpai.api

import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.GetConfigResponse
import org.appdevncsu.gpai.api.models.Question
import org.appdevncsu.gpai.api.models.SignInRequest
import org.appdevncsu.gpai.api.models.SignInResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface Api {

    @POST("/api/chat")
    @Headers("Content-Type: application/json")
    suspend fun askQuestion(
        @Body question: Question
    ): Response<Answer>

    @POST("/api/sign-in")
    @Headers("Content-Type: application/json")
    suspend fun signIn(
        @Body request: SignInRequest
    ): Response<SignInResponse>

    @GET("/api/config")
    @Headers("Content-Type: application/json")
    suspend fun getConfig(): Response<GetConfigResponse>

}