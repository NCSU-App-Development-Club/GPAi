package org.appdevncsu.gpai

import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import junit.framework.TestCase.assertTrue
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.ResponseBody.Companion.toResponseBody
import org.appdevncsu.gpai.api.Api
import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.api.models.Question
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import retrofit2.Response

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
            message = "Artificial Intelligence..."
        )

        // retrofit successful response
        whenever(api.askQuestion(question)).thenReturn(Response.success(answer))

        val response = api.askQuestion(question)

        assertTrue(response.isSuccessful)
        assertEquals("Artificial Intelligence...", response.body()?.message)
    }


    @Test
    fun `mock API error response`() = runTest {
        val question = Question(
            messages = listOf(
                Message(role = "user", content = "This will fail")
            )
        )
        val errorBody =
            "{\"error\":\"Server Error\"}".toResponseBody("application/json".toMediaTypeOrNull())
        val errorResponse = Response.error<Answer>(500, errorBody)

        whenever(api.askQuestion(question)).thenReturn(errorResponse)

        val response = api.askQuestion(question)

        assertFalse(response.isSuccessful)
        assertEquals(500, response.code())
    }
}