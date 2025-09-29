package org.appdevncsu.gpai

import org.appdevncsu.gpai.utils.PDFUtils
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests for the PDFUtils class, specifically testing the transcript parsing functionality.
 */
class PDFUtilsTest {

    /**
     * Tests the successful parsing of a transcript containing two terms and several courses.
     *
     * The test validates:
     * - Parsing of two terms: "2021 Fall Term" and "2022 Spring Term".
     * - Correct number of courses per term (2 per term).
     * - Accurate parsing of course details including course code, course name, credits attempted,
     *   credits earned, grade, and quality points.
     */
    @Test
    fun testParseTranscript_success() {
        val transcriptText = """
            Beginning of Undergraduate Record
            2021 Fall Term
            CSC 101   Introduction to Programming     3.000   3.000   A-    11.000
            MATH 225  Calculus II                     4.000   4.000   B+   13.333
            
            2022 Spring Term
            CSC 216   Software Development            3.000   3.000   A     12.000
            ENG 102   Composition and Rhetoric        3.000   3.000   B     9.000
            """.trimIndent()

        val transcript = PDFUtils.parseTranscript(transcriptText)

        // Assertions for term count and individual term details
        assertEquals(2, transcript.terms.size)

        val fall2021Term = transcript.terms[0]
        assertEquals("2021 Fall Term", fall2021Term.name)
        assertEquals(2, fall2021Term.courses.size)

        // Assertions for course details in Fall 2021 term
        assertEquals("Introduction to Programming", fall2021Term.courses[0].courseName)
        assertEquals(3, fall2021Term.courses[0].attempted)
        assertEquals(3, fall2021Term.courses[0].earned)
        assertEquals("A-", fall2021Term.courses[0].grade)
        assertEquals(11.0, fall2021Term.courses[0].points, 0.001)

        assertEquals("Calculus II", fall2021Term.courses[1].courseName)
        assertEquals(4, fall2021Term.courses[1].attempted)
        assertEquals(4, fall2021Term.courses[1].earned)
        assertEquals("B+", fall2021Term.courses[1].grade)
        assertEquals(13.333, fall2021Term.courses[1].points, 0.001)

        val spring2022Term = transcript.terms[1]
        assertEquals("2022 Spring Term", spring2022Term.name)
        assertEquals(2, spring2022Term.courses.size)

        // Assertions for course details in Spring 2022 term
        assertEquals("Software Development", spring2022Term.courses[0].courseName)
        assertEquals(3, spring2022Term.courses[0].attempted)
        assertEquals(3, spring2022Term.courses[0].earned)
        assertEquals("A", spring2022Term.courses[0].grade)
        assertEquals(12.0, spring2022Term.courses[0].points, 0.001)

        assertEquals("Composition and Rhetoric", spring2022Term.courses[1].courseName)
        assertEquals(3, spring2022Term.courses[1].attempted)
        assertEquals(3, spring2022Term.courses[1].earned)
        assertEquals("B", spring2022Term.courses[1].grade)
        assertEquals(9.0, spring2022Term.courses[1].points, 0.001)

        assertEquals(13, transcript.totalCredits)
        assertEquals(45.333, transcript.totalEarnedPoints, 0.001)
        assertEquals(3.487, transcript.gpa, 0.001)

        print(transcript)
    }

    /**
     * Tests the parsing of an empty transcript string.
     *
     * This test ensures that when an empty transcript text is provided, the resulting
     * Transcript object contains no terms.
     */
    @Test
    fun testParseTranscript_empty() {
        val transcriptText = ""
        val transcript = PDFUtils.parseTranscript(transcriptText)
        assertEquals(0, transcript.terms.size)
        assertEquals(0, transcript.totalCredits)
        assertEquals(0.0, transcript.totalEarnedPoints, 0.001)
        assertEquals(0.0, transcript.gpa, 0.001)
    }
}
