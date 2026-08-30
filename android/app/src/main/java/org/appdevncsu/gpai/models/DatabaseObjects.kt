package org.appdevncsu.gpai.models

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import org.appdevncsu.gpai.api.models.Message

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
        onDelete = ForeignKey.CASCADE // When this Course's Term is deleted, delete the Course automatically
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

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey
    val id: String,
    val role: String,
    val content: String,
    val isContext: Boolean = false,
    val signature: String? = null,
    /** Ordering column so messages are restored in the correct sequence. */
    val position: Int = 0,
    val isFlagged: Boolean = false,
) {
    fun toMessage() = Message(
        id = id,
        role = role,
        content = content,
        isContext = isContext,
        signature = signature,
        isFlagged = isFlagged,
    )

    companion object {
        fun from(message: Message, position: Int) = ChatMessageEntity(
            id = message.id,
            role = message.role,
            content = message.content,
            isContext = message.isContext,
            signature = message.signature,
            position = position,
            isFlagged = message.isFlagged,
        )
    }
}
