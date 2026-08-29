package org.appdevncsu.gpai.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import org.appdevncsu.gpai.models.ChatMessageEntity

@Dao
interface ChatDao {

    @Query("SELECT * FROM chat_messages ORDER BY position ASC")
    suspend fun getAllMessages(): List<ChatMessageEntity>

    @Transaction
    suspend fun replaceAll(messages: List<ChatMessageEntity>) {
        deleteAll()
        insertAll(messages)
    }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(messages: List<ChatMessageEntity>)

    @Query("DELETE FROM chat_messages")
    suspend fun deleteAll()
}
