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
    suspend fun signIn(signInRequest: SignInRequest): Result<SignInResponse>

    /**
     * Gets configuration needed to send Google authorization requests (the Client ID)
     */
    suspend fun getConfig(): Result<GetConfigResponse>

    /**
     * Revokes the current server-side session. The local credentials must be
     * cleared separately by the caller.
     */
    suspend fun signOut(): Result<Unit>

    /**
     * Deletes the user's account and all associated server-side data.
     * The local credentials must be cleared separately by the caller.
     */
    suspend fun deleteAccount(): Result<Unit>

    /**
     * Reports an assistant message as offensive or inappropriate.
     */
    suspend fun flagMessage(message: Message, reason: String): Result<Unit>
}