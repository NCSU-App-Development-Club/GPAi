package org.appdevncsu.gpai.api.repositories

import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.api.models.Question
import org.appdevncsu.gpai.api.Api
import org.appdevncsu.gpai.api.AuthorizationInterceptor
import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.GetConfigResponse
import org.appdevncsu.gpai.api.models.SignInRequest
import org.appdevncsu.gpai.api.models.SignInResponse

class RepositoryImpl(private val api: Api) : Repository {

    override suspend fun askQuestion(
        messages: List<Message>
    ): Result<Answer> {
        try {
            api.askQuestion(
                question = Question(messages = messages)
            ).also { response ->
                return if (response.isSuccessful) {
                    Result.success(response.body()!!)
                } else {
                    Result.failure(RuntimeException(response.errorBody()?.string().toString()))
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    /**
     * Thrown when the user signs in with a non-NCSU Google account.
     */
    class InvalidDomainException : RuntimeException()

    override suspend fun signIn(signInRequest: SignInRequest): Result<SignInResponse> {
        try {
            val response = api.signIn(signInRequest)

            return if (response.isSuccessful) {
                val body = response.body()!!
                AuthorizationInterceptor.setToken(body.sessionID)
                Result.success(body)
            } else {
                val str = response.errorBody()?.string().toString()
                if (str.contains("Invalid domain")) {
                    Result.failure(InvalidDomainException())
                } else {
                    Result.failure(RuntimeException(str))
                }
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }

    override suspend fun getConfig(): Result<GetConfigResponse> {
        try {
            val response = api.getConfig()

            return if (response.isSuccessful) {
                val body = response.body()!!
                Result.success(body)
            } else {
                Result.failure(RuntimeException(response.errorBody()?.string().toString()))
            }
        } catch (e: Exception) {
            return Result.failure(e)
        }
    }
}
