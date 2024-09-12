package com.adc.gpai.models

import kotlinx.serialization.Serializable

@Serializable
/**
 * Data class representing a course taken in a specific term.
 *
 * @param term The academic term during which the course was taken (e.g., "Fall 2023").
 * @param courseCode The unique code of the course (e.g., "CS 101").
 * @param courseName The full name of the course (e.g., "Introduction to Programming").
 * @param attempted The number of credits attempted for this course.
 * @param earned The number of credits earned for this course.
 * @param points The number of grade points earned for this course (used in GPA calculation).
 * @param grade The final grade received in the course (e.g., "A", "B+", etc.).
 */
data class Course(
    val term: String,
    val courseCode: String,
    val courseName: String,
    val attempted: Int,
    val earned: Int,
    val points: Double,
    val grade: String
)

