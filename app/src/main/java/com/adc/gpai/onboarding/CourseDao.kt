package com.adc.gpai.onboarding

import android.icu.text.MessagePattern.ArgType.SELECT
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.adc.gpai.models.Course
import com.adc.gpai.home.ForecasterScreen

@Dao
interface CourseDao {
    //TODO: Add methods for persisting and modifying transcript data
    //TODO: How to implement in app?
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(courseName: Course)

    //Update entire course
    @Update
    suspend fun updateCourse(course: Course)

    // Check if a course exists by courseCode
    @Query("SELECT COUNT(*) FROM Course WHERE courseCode = :courseCode")
    suspend fun courseExists(courseCode: String): Int

    @Query("UPDATE Course SET courseCode = :newCourseCode WHERE courseCode = :courseCode")
    suspend fun updateCourseCode(newCourseCode: String, courseCode: String)

    @Query("UPDATE Course SET courseName = :newCourseName WHERE courseCode = :courseCode")
    suspend fun updateCourseName(newCourseName: String, courseCode: String)

    @Query("UPDATE Course SET attempted = :newCreditHours WHERE courseCode = :courseCode")
    suspend fun updateHoursAttempted(newCreditHours: Int, courseCode : String)

    @Query("UPDATE Course SET earned = :newCreditHours WHERE courseCode = :courseCode")
    suspend fun updateHoursEarned(newCreditHours: Int, courseCode : String)

    @Query("UPDATE Course SET grade = :newGrade WHERE courseCode = :courseCode")
    suspend fun updateGrade(newGrade: Int, courseCode: String)

    @Query("UPDATE Course SET points = :newPoints WHERE courseCode = :courseCode")
    suspend fun updatePoints(newPoints: Double, courseCode: String)

    @Delete
    suspend fun deleteCourse(courseName: Course)


    //I DONT THINK THIS IS CORRECT
    @Query("SELECT * FROM Course")
    suspend fun getAllCourses() : List<Course>


}

