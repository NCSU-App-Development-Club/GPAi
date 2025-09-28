package org.appdevncsu.gpai.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation

@Entity(tableName = "terms")
data class TermDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String = "",
) {
    companion object {
        fun from(term: Term) = TermDTO(0, term.name)
    }
}

data class TermWithCoursesDTO(
    @Embedded val term: TermDTO,
    @Relation(
        entityColumn = "termId",
        parentColumn = "id",
    )
    val courses: List<CourseDTO>
) {
    fun toTerm() = Term(term.id, term.name, courses.map(CourseDTO::toCourse))
}

fun Collection<TermWithCoursesDTO>.toTranscript() = Transcript(map(TermWithCoursesDTO::toTerm))

@Entity(
    tableName = "courses",
    foreignKeys = [ForeignKey(
        entity = TermDTO::class,
        parentColumns = ["id"],
        childColumns = ["termId"],
        onDelete = ForeignKey.Companion.CASCADE // When this Course's Term is deleted, delete the Course automatically
    )]
)
data class CourseDTO(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val termId: Int = 0,
    val courseCode: String = "",
    val courseName: String,
    val attempted: Int = 0,
    val earned: Int = 0,
    val points: Double,
    val grade: String
) {
    fun toCourse() = Course(id, courseCode, courseName, attempted, earned, points, grade)

    companion object {
        fun from(course: Course, termId: Int) = CourseDTO(
            0,
            termId,
            course.courseCode,
            course.courseName,
            course.attempted,
            course.earned,
            course.points,
            course.grade
        )
    }
}

@Entity(tableName = "users")
data class UserDTO(
    @PrimaryKey
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val photoURL: String = "",
    val token: String = ""
) {
    fun toUser() = User(name, email, id, photoURL, token)

    companion object {
        fun from(user: User) = UserDTO(
            user.id,
            user.name,
            user.email,
            user.photoURL,
            user.token
        )
    }
}
