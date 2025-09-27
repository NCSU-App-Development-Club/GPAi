package org.appdevncsu.gpai.api.repositories

import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.BaseModel
import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.api.models.Question
import org.appdevncsu.gpai.api.Api

class RepositoryImpl(private val api: Api) : Repository {

    override suspend fun askQuestion(
        prevQuestion: List<Message>,
        question: String
    ): BaseModel<Answer> {
        try {
            api.askQuestion(
                question = Question(
                    messages = prevQuestion + Message(
                        role = "user",
                        content = question
                    )
                )
            ).also { response ->
                return if (response.isSuccessful) {
                    BaseModel.Success(data = response.body()!!)
                } else {
                    BaseModel.Error(response.errorBody()?.string().toString())
                }
            }
        } catch (e: Exception) {
            return BaseModel.Error(e.message.toString())
        }
    }
}