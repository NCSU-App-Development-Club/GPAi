package org.appdevncsu.gpai.api.models

data class Question(
    val messages: List<Message>
)

data class Message(
    /**
     * "user", "system" or "assistant"
     */
    val role: String,
    val content: String
)

data class Answer(
    val message: String
)
