package com.adc.gpai.models

import kotlinx.serialization.Serializable

/**
 * Data class representing a course taken in a specific term.
 * Transient fields are not serialized to JSON.
 * @param id A unique ID for the course. Used for local database persistence. Omit or specify 0 to automatically generate one.
 * @param courseCode The unique code of the course (e.g., "CS 101").
 * @param courseName The full name of the course (e.g., "Introduction to Programming").
 * @param attempted The number of credits attempted for this course.
 * @param earned The number of credits earned for this course.
 * @param points The number of grade points earned for this course (used in GPA calculation).
 * @param grade The final grade received in the course (e.g., "A", "B+", etc.).
 */
@Serializable
data class Course(
    val id: Int,
    val courseCode: String = "",
    val courseName: String,
    val attempted: Int = 0,
    val earned: Int = 0,
    val points: Double,
    val grade: String
) {
    /**
     * Creates a new Course with an automatically-generated ID.
     */
    constructor(
        courseCode: String = "",
        courseName: String,
        attempted: Int = 0,
        earned: Int = 0,
        points: Double,
        grade: String
    ) : this(0, courseCode, courseName, attempted, earned, points, grade)

    /**
     * Returns whether the course should be included in the GPA calculation.
     * Grades that should count: A+, A, A-, B+, ..., F
     * Grades that shouldn't count: S, U, CR, AU, NR, IN, LA, W
     */
    fun isForGrade(): Boolean {
        return this.grade.first() in 'A'..'F'
    }
}
