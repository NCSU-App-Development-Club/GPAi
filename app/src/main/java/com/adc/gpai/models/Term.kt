package com.adc.gpai.models

/**
 * Data class representing an academic term that contains a list of courses.
 *
 * @param id A unique ID for the term. Used for local database persistence when linking courses to this term.
 * @param name The name of the academic term (e.g., "Fall 2023").
 * @param courses A list of courses taken during this term.
 */
data class Term(
    val id: Int,
    val name: String,
    val courses: List<Course>
) {
    /**
     * Creates a new Term with an automatically-generated ID.
     */
    constructor(name: String, courses: List<Course>) : this(0, name, courses)
}