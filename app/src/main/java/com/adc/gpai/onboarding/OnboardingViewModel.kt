package com.adc.gpai.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.adc.gpai.models.Course
import com.adc.gpai.models.Term
import com.adc.gpai.models.Transcript

class OnboardingViewModel(): ViewModel() {
    //TODO: Add methods for persisting and modifying transcript data

    constructor(transcript: Transcript) : this() {
        this.updateTranscript(transcript)
    }

    private val _transcript = MutableLiveData<Transcript>(null)
    val transcript: LiveData<Transcript> = _transcript

    fun updateTranscript(transcript: Transcript) {
        _transcript.value = transcript
    }

    companion object {
        // Sample data for previewing the UI during development.
        val sampleTranscript = Transcript(
            listOf(
                Term(
                    "Fall 2023",
                    listOf(
                        Course("CS 101", "Introduction to Programming", 3, 3, 12.0, "A"),
                        Course( "MA 200", "Calculus I", 3, 3, 9.0, "B+")
                    )
                ),
                Term(
                    "Spring 2024",
                    listOf(
                        Course( "CS 201", "Data Structures", 4, 4, 14.0, "A-"),
                        Course( "PH 101", "Physics I", 3, 3, 9.0, "B")
                    )
                )
            )
        )
    }
}