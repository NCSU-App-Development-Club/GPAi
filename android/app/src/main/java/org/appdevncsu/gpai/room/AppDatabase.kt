package org.appdevncsu.gpai.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import org.appdevncsu.gpai.models.CourseDTO
import org.appdevncsu.gpai.models.TermDTO
import org.appdevncsu.gpai.models.UserDTO

@Database(entities = [CourseDTO::class, TermDTO::class, UserDTO::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun termCourseDao(): TermCourseDao
    abstract fun userDao(): UserDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
