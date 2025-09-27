package org.appdevncsu.gpai.api.repositories

import org.appdevncsu.gpai.api.models.Answer
import org.appdevncsu.gpai.api.models.BaseModel
import org.appdevncsu.gpai.api.models.Message

interface Repository {

    suspend fun askQuestion(prevQuestion: List<Message>, question: String): BaseModel<Answer>

}