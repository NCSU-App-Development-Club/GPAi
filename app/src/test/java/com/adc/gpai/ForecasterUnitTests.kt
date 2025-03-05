package com.adc.gpai

import org.junit.Before
import org.junit.Test

/**
 * Unit tests for the Forecaster functionality, such as GPA calculations
 */
class ForecasterUnitTests {
    // private lateinit var forecaster: Forecaster

    @Before
    fun setUp() {
        //  forecaster = Forecaster()
    }

    /**
     *
     */
    @Test
    fun testGPACalcValid() {
//        val courses = listOf(
//            Course("CS101", "Intro to Programming", attempted = 3, earned = 3, points = 12.0, grade = "A"),
//            Course("MATH201", "Calculus II", attempted = 4, earned = 4, points = 12.0, grade = "B")
//        )
        // val gpa = forecaster.calculateGPA(courses)
        // assertEquals(3.43, gpa, 0.01)
    }

    @Test
    fun testGPACalcNoInput() {
//        val courses = emptyList<Course>()
//        val gpa = forecaster.calculateGPA(courses)
//        assertEquals(0.0, gpa, 0.0)
    }

    @Test
    fun testGPACalcInvalidInputs() {
        // //Testing forecaster with negative credit hours
//        val courses = listOf(
//            Course("ENG102", "English Literature", attempted = -3, earned = 3, points = 9.0, grade = "B")  // Invalid negative credit hours
//        )
//        assertThrows(IllegalArgumentException::class.java) {
//            forecaster.calculateGPA(courses)
//        }

//        //Testing forecaster with division by 0
//        val courses2 = listOf(
//            Course("HIST200", "History", attempted = 0, earned = 0, points = 0.0, grade = "A")
//        )
//        val gpa = forecaster.calculateGPA(courses2)
//        assertEquals(0.0, gpa, 0.0)
//
//        //Testing forecaster with missing grade
//        val courses3 = listOf(
//            Course("CHEM101", "Chemistry", attempted = 3, earned = 3, points = 12.0, grade = "A"),
//            Course("BIO202", "Advanced Biology", attempted = 3, earned = 3, points = 0.0, grade = "")  // Missing grade
//        )
//        assertThrows(IllegalArgumentException::class.java) {
//            forecaster.calculateGPA(courses3)
//        }
//
//        //Testing forecaster with a "W" grade
//        val courses = listOf(
//            Course("PHYS300", "Quantum Mechanics", attempted = 3, earned = 3, points = 12.0, grade = "A"),
//            Course("MATH450", "Topology", attempted = 3, earned = 3, points = 0.0, grade = "W")  // Withdrawn course
//        )
//        val gpa2 = forecaster.calculateGPA(courses)
//        assertEquals(4.0, gpa2, 0.01)
//
//        //Testing forecaster with a lot of courses at once
//        val manyCourses = List(100) { i ->
//            Course("COURSE$i", "Course $i", attempted = 3, earned = 3, points = 9.0, grade = "B")
//        }
//        val manyGpa = forecaster.calculateGPA(manyCourses)
//        assertEquals(3.0, manyGpa, 0.01)
    }

    @Test
    fun testGPACalcBoundCases() {
//        val courses = listOf(
//            Course("BIO101", "Biology", attempted = 3, earned = 3, points = 0.0, grade = "F"),
//            Course("PHY102", "Physics", attempted = 3, earned = 3, points = 12.999, grade = "A+")
//        )
//        val gpa = forecaster.calculateGPA(courses)
//        assertEquals(2.17, gpa, 0.01)
    }

    @Test
    fun testGPACalcRounding() {
//        val courses = listOf(
//            Course("STAT400", "Statistics", attempted = 3, earned = 3, points = 9.999, grade = "B+"),
//            Course("CS102", "Data Structures", attempted = 3, earned = 3, points = 9.0, grade = "B")
//        )
//        val gpa = forecaster.calculateGPA(courses)
//        assertEquals(3.17, gpa, 0.01)
    }
}
