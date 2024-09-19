package com.adc.gpai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
/**
 * Data class representing an academic term that contains a list of courses.
 *
 * @param name The name of the academic term (e.g., "Fall 2023").
 * @param courses A list of courses taken during this term.
 */
data class Term(
    val name: String,
    val courses: List<Course>
)
