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
    val content: String,
    /**
     * Marks the synthetic transcript/context message. The model sees it as a user message,
     * but it's always hidden in the UI, and it gets updated whenever the transcript changes.
     */
    val isContext: Boolean = false,
    /**
     * Server-issued HMAC signature for assistant messages. Sent back to the server on subsequent
     * requests so it can verify the assistant message was not tampered with locally. `null` for
     * user messages.
     */
    val signature: String? = null,
    val isFlagged: Boolean = false,
)

data class Answer(
    val message: String,
    val signature: String? = null
)

data class FlagRequest(
    val messageId: String,
    val content: String,
    val signature: String,
    val reason: String,
)
