package org.appdevncsu.gpai.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import org.appdevncsu.gpai.models.Course
import org.appdevncsu.gpai.models.CourseDTO
import org.appdevncsu.gpai.models.Transcript
import org.appdevncsu.gpai.models.toTranscript
import kotlinx.coroutines.launch

/**
 * A [ViewModel] that keeps track of the user's [Transcript].
 * To get an instance of this class, use Koin:
 * ```kotlin
 * val viewModel: TranscriptRepository = koinViewModel()
 * ```
 *
 * When an instance of this class is constructed, it will automatically
 * pull the user's courses from Room. Then, any subsequent updates will
 * update the ViewModel's state and then immediately sync them to Room.
 */
class TranscriptRepository(private val database: AppDatabase) : ViewModel() {
    private val _transcript = MutableLiveData<Transcript>()
    val transcript: LiveData<Transcript> get() = _transcript

    init {
        fetchAllCourses()
    }

    fun updateCourse(course: Course) {
        // Optimistic update
        _transcript.value = _transcript.value?.copy(terms = _transcript.value!!.terms.map {
            it.copy(courses = it.courses.map { otherCourse ->
                if (otherCourse.id == course.id) course
                else otherCourse
            })
        })
        // Update in the DB
        viewModelScope.launch {
            database.termCourseDao().updateCourse(course)
            fetchAllCourses() // Refresh the courses list to keep in sync (just in case the update didn't work)
        }
    }

    fun removeCourse(course: Course) {
        // Optimistic update
        _transcript.value = _transcript.value?.copy(terms = _transcript.value!!.terms.map {
            it.copy(courses = it.courses.filter { otherCourse -> otherCourse.id != course.id })
        })
        // Update in the DB
        viewModelScope.launch {
            database.termCourseDao().deleteCourse(course.id)
            fetchAllCourses() // Refresh the courses list to keep in sync (just in case the update didn't work)
        }
    }

    fun addCourse(termId: Int, course: Course) {
        // Optimistic update - add the course to the term with matching ID
        _transcript.value = _transcript.value?.copy(terms = _transcript.value!!.terms.map { term ->
            if (term.id == termId) {
                term.copy(courses = term.courses + course)
            } else {
                term
            }
        })
        
        // Update in the DB
        viewModelScope.launch {
            database.termCourseDao().insertCourse(CourseDTO.Companion.from(course, termId))
            fetchAllCourses() // Refresh the courses list to keep in sync
        }
    }

    private fun fetchAllCourses() {
        viewModelScope.launch {
            _transcript.value = database.termCourseDao().getAllTerms().toTranscript()
        }
    }

    /**
     * Removes all existing terms and courses and inserts new ones from the provided [transcript].
     */
    fun updateTranscript(transcript: Transcript) {
        _transcript.value = transcript // Update the value immediately while we're saving it
        viewModelScope.launch {
            database.termCourseDao().writeTranscript(transcript)
            fetchAllCourses()
        }
    }
}
