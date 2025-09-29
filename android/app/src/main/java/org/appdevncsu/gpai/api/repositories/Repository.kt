package org.appdevncsu.gpai.api.repositories

import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.GetConfigResponse
import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.api.models.SignInRequest
import org.appdevncsu.gpai.api.models.SignInResponse

interface Repository {

    /**
     * Uses the current authorization (see [signIn]) to send an AI chat request.
     */
    suspend fun askQuestion(messages: List<Message>): Result<Answer>

    /**
     * Creates a GPAi session for the user and returns the session token
     */
    suspend fun signIn(googleToken: SignInRequest): Result<SignInResponse>

    /**
     * Gets configuration needed to send Google authorization requests (the Client ID)
     */
    suspend fun getConfig(): Result<GetConfigResponse>
}