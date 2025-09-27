package org.appdevncsu.gpai.onboarding

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import org.appdevncsu.gpai.models.Course
import org.appdevncsu.gpai.models.CourseDTO
import org.appdevncsu.gpai.models.TermDTO
import org.appdevncsu.gpai.models.TermWithCoursesDTO
import org.appdevncsu.gpai.models.Transcript

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
            val termId = insertTerm(TermDTO.Companion.from(term)).toInt()
            term.courses.forEach { course ->
                insertCourse(CourseDTO.Companion.from(course, termId))
            }
        }
    }
}

