package com.haw.takonappcompose.repositories

import com.haw.takonappcompose.models.Answer
import com.haw.takonappcompose.models.BaseModel
import com.haw.takonappcompose.models.Message
import com.haw.takonappcompose.models.Question
import com.haw.takonappcompose.network.Api
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

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