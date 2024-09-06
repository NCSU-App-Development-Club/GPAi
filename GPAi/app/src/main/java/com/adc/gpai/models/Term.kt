package com.adc.gpai.models

import kotlinx.serialization.Serializable

@Serializable
data class Term(
    val name: String,
    val courses: List<Course>
)