package com.adc.gpai.models

import kotlinx.serialization.Serializable

@Serializable
data class Course(
    val term: String,
    val courseCode: String,
    val courseName: String,
    val attempted: Int,
    val earned: Int,
    val points: Double,
    val grade: String
)
