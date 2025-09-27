package com.adc.gpai.api.repositories

import com.adc.gpai.api.models.Answer
import com.adc.gpai.api.models.BaseModel
import com.adc.gpai.api.models.Message

interface Repository {

    suspend fun askQuestion(prevQuestion: List<Message>, question: String): BaseModel<Answer>

}