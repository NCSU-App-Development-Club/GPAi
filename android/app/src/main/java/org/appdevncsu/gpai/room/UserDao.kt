package org.appdevncsu.gpai.room

import androidx.room.Dao
import androidx.room.Query
import org.appdevncsu.gpai.models.UserDTO

@Dao
interface UserDao {

    // Yes, I know these queries have no WHERE clauses on them. There should only ever be one row in this table.

    @Query("SELECT * FROM users ORDER BY updatedAt DESC LIMIT 1")
    suspend fun getUser(): UserDTO?

    @Query("INSERT INTO users (id, name, email, photoURL, token, updatedAt) VALUES (:id, :name, :email, :photoURL, :token, datetime()) ON CONFLICT DO UPDATE SET id = :id, name = :name, email = :email, token = :token, updatedAt = datetime()")
    suspend fun setUser(id: String, name: String, email: String, photoURL: String, token: String)

    suspend fun setUser(user: UserDTO) {
        setUser(user.id, user.name, user.email, user.photoURL, user.token)
    }
}
