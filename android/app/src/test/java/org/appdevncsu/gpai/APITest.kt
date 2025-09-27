package org.appdevncsu.gpai

import org.appdevncsu.gpai.api.Api
import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.Choice
import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.api.models.Question
import org.appdevncsu.gpai.api.models.Usage
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.ResponseBody
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response
import okhttp3.MediaType.Companion.toMediaTypeOrNull

class APITest {
    private lateinit var api: Api

    @Before
    fun setup() {
        api = mock()
    }

    @Test
    fun `mock successful question response`() = runTest {
        val question = Question(
            messages = listOf(
                Message(role = "user", content = "What is AI?")
            )
        )
        val answer = Answer(
            id = "chatcmpl-123",
            `object` = "chat.completion",
            created = System.currentTimeMillis(),
            model = "gpt-3.5-turbo",
            choices = listOf(
                Choice(
                    index = 0,
                    message = Message(role = "assistant", content = "Artificial Intelligence..."),
                    finishReason = "stop"
                )
            ),
            usage = Usage(
                promptTokens = 10,
                completionTokens = 20,
                totalTokens = 30
            )
        )

        // retrofit successful response
        whenever(api.askQuestion(question)).thenReturn(Response.success(answer))

        val response = api.askQuestion(question)

        assertTrue(response.isSuccessful)
        assertEquals("Artificial Intelligence...", response.body()?.choices?.first()?.message?.content)
    }


    @Test
    fun `mock API error response`() = runTest {
        val question = Question(
            messages = listOf(
                Message(role = "user", content = "This will fail")
            )
        )
        val errorBody = ResponseBody.create("application/json".toMediaTypeOrNull(), "{\"error\":\"Server Error\"}")
        val errorResponse = Response.error<Answer>(500, errorBody)

        whenever(api.askQuestion(question)).thenReturn(errorResponse)

        val response = api.askQuestion(question)

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }
}