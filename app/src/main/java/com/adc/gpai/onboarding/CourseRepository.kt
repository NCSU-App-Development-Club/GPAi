package com.adc.gpai.onboarding

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.room.*
import com.adc.gpai.models.Course
import kotlinx.coroutines.launch


class CourseRepository (private val database:AppDatabase): ViewModel() {
    //TODO: Add methods for persisting and modifying transcript data
    //TODO: ADD RETURNS FOR IF COURSE CODE NOT FOUND

    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> get() = _courses

    fun addCourse(course: Course) {
        viewModelScope.launch {
            val existingCount = database.courseDao().courseExists(course.courseCode)
            if (existingCount == 0) {
                database.courseDao().insertCourse(course)
                fetchAllCourses()
            }
        }
    }

    fun updateCourse(course: Course) {
        viewModelScope.launch {
            database.courseDao().updateCourse(course)
            fetchAllCourses()
        }

    }

    fun updateCourseCode(newCourseCode: String, courseCode: String) {
        viewModelScope.launch {
            database.courseDao().updateCourseCode(newCourseCode, courseCode)
            fetchAllCourses()
        }
    }


    fun updateCourseName(newCourseName: String, courseCode: String) {
        viewModelScope.launch {
            database.courseDao().updateCourseName(newCourseName, courseCode)
            fetchAllCourses()
        }
    }


    fun updateHoursAttempted(hours: Int, courseCode: String) {
        viewModelScope.launch {
            database.courseDao().updateHoursAttempted(hours, courseCode)
            fetchAllCourses()
        }
    }

    fun updateHoursEarned(hours: Int, courseCode: String) {
        viewModelScope.launch {
            database.courseDao().updateHoursEarned(hours, courseCode)
            fetchAllCourses()
        }
    }

    fun updatePoints(points: Double, courseCode: String) {
        viewModelScope.launch {
            database.courseDao().updatePoints(points, courseCode)
            fetchAllCourses()
        }
    }

    fun updateGrade(grade: Int, courseCode: String) {
        viewModelScope.launch {
            database.courseDao().updateGrade(grade, courseCode)
            fetchAllCourses()
        }
    }

    fun deleteCourse(course: Course) {
        viewModelScope.launch {
            database.courseDao().deleteCourse(course)
            fetchAllCourses()
        }
    }


    //MAY BE WRONG
    private fun fetchAllCourses() {
        viewModelScope.launch {
            _courses.value = database.courseDao().getAllCourses()
        }
    }
}









/*These are examples in case you need them for other methods
@Query("SELECT * FROM course WHERE courseName = :courseName")
 suspend fun getCourseById(courseName: String): Course?{

 }


 @Query("SELECT * FROM course")
 fun getAllCourses(): List<Course>{

 }

 @Query("SELECT * FROM course WHERE grade = :grade")
 fun getCoursesByGrade(grade: String): List<Course>{

 }

 */


