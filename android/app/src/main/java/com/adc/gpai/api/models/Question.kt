package com.adc.gpai.api.models

data class Question(
    val model: String = "gpt-3.5-turbo",
    val messages: List<Message>
)

data class Message(
    /**
     * "user", "system" or "assistant"
     */
    val role: String,
    val content: String
)
