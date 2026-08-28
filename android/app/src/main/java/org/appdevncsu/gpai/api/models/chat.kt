package org.appdevncsu.gpai.api.models

import kotlin.jvm.Transient
import java.util.UUID

data class Question(
    val messages: List<Message>
)

data class Message(
    @Transient
    val id: String = UUID.randomUUID().toString(),
    /**
     * "user", "system" or "assistant"
     */
    val role: String,
    val content: String
)

data class Answer(
    val message: String
)
