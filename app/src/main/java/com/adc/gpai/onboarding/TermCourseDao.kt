package com.adc.gpai.onboarding

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.adc.gpai.models.Course
import com.adc.gpai.models.CourseDTO
import com.adc.gpai.models.TermDTO
import com.adc.gpai.models.TermWithCoursesDTO
import com.adc.gpai.models.Transcript

@Dao
interface TermCourseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTerm(term: TermDTO): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCourse(course: CourseDTO)

    suspend fun updateCourse(course: Course) =
        updateCourse(
            course.id,
            course.courseCode,
            course.courseName,
            course.attempted,
            course.earned,
            course.points,
            course.grade
        )

    @Query("UPDATE courses SET courseCode = :courseCode, courseName = :courseName, attempted = :attempted, earned = :earned, points = :points, grade = :grade WHERE id = :id")
    suspend fun updateCourse(
        id: Int,
        courseCode: String = "",
        courseName: String,
        attempted: Int = 0,
        earned: Int = 0,
        points: Double,
        grade: String
    )

    @Query("DELETE FROM courses WHERE id = :id")
    suspend fun deleteCourse(id: Int)

    @Transaction
    @Query("SELECT * FROM terms")
    suspend fun getAllTerms(): List<TermWithCoursesDTO>

    @Query("DELETE FROM terms")
    suspend fun truncate()

    @Update
    suspend fun updateTerm(term: TermDTO)

    @Transaction
    suspend fun writeTranscript(transcript: Transcript) {
        truncate() // Clear ALL terms and courses
        transcript.terms.forEach { term ->
            val termId = insertTerm(TermDTO.from(term)).toInt()
            term.courses.forEach { course ->
                insertCourse(CourseDTO.from(course, termId))
            }
        }
    }
}

