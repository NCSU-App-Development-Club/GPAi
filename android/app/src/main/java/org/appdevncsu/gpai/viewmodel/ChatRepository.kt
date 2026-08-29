package org.appdevncsu.gpai.viewmodel

import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.models.ChatMessageEntity
import org.appdevncsu.gpai.room.AppDatabase

/**
 * Handles reading and writing advisor chat messages to Room.
 */
class ChatRepository(database: AppDatabase) {

    private val chatDao = database.chatDao()

    suspend fun loadMessages(): List<Message> {
        return chatDao.getAllMessages().map(ChatMessageEntity::toMessage)
    }

    suspend fun saveMessages(messages: List<Message>) {
        chatDao.replaceAll(
            messages.mapIndexed { index, message ->
                ChatMessageEntity.from(message, index)
            }
        )
    }
}
