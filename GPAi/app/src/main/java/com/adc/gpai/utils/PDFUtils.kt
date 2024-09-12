package com.adc.gpai.utils

import android.content.Context
import android.net.Uri
import com.adc.gpai.models.Course
import com.adc.gpai.models.Term
import com.adc.gpai.models.Transcript
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import com.tom_roush.pdfbox.pdmodel.PDDocument
import com.tom_roush.pdfbox.text.PDFTextStripper
import java.io.InputStream

/**
 * Utils class containing pdf parsing and utility functions for the application.
 */
public class PDFUtils {
    companion object {

        /**
         * Reads text from a PDF file located at the given URI.
         * @param context The application context.
         * @param pdfUri The URI of the PDF file.
         * @return The extracted text from the PDF file, or null if an error occurred.
         */
        fun readTextFromPdf(context: Context, pdfUri: Uri): String? {
            PDFBoxResourceLoader.init(context)
            var document: PDDocument? = null
            return try {
                // Open the PDF file as an InputStream
                val inputStream: InputStream? = context.contentResolver.openInputStream(pdfUri)
                // Load the document from the InputStream
                document = PDDocument.load(inputStream)
                // Use PDFTextStripper to extract text
                val pdfStripper = PDFTextStripper()
                pdfStripper.getText(document)
            } catch (e: Exception) {
                e.printStackTrace()
                null
            } finally {
                // Close the document
                document?.close()
            }
        }

        /**
         * Parses a transcript text into a Transcript object.
         * @param transcriptText The text representing the transcript.
         * @return The parsed Transcript object.
         */
        fun parseTranscript(transcriptText: String): Transcript {
            val termPattern = Regex("""\d{4} \w+ Term""") // Pattern to identify terms (e.g., "2021 Fall Term")
            val coursePattern = Regex("""([A-Z]{1,4}\s+\d{3}\**)\s+([\w\s\-&]+)\s+(\d+\.\d{3})\s+(\d+\.\d{3})\s+([A-Z\+\-]*)\s+(\d+\.\d{3})""")

            val terms = mutableListOf<Term>()
            val lines = transcriptText.split("\n")
            var currentTerm: String? = null
            val currentCourses = mutableListOf<Course>()

            // Flag to indicate if parsing should start
            var startParsing = false

            for (line in lines) {
                // Check if we should start parsing
                if (!startParsing) {
                    if (line.contains("Beginning of Undergraduate Record")) {
                        startParsing = true
                    }
                    continue // Skip all lines until we find the start point
                }

                // Identify a new term
                val termMatch = termPattern.find(line)
                if (termMatch != null) {
                    // If we have a current term with courses, save it
                    if (currentTerm != null && currentCourses.isNotEmpty()) {
                        terms.add(Term(name = currentTerm, courses = currentCourses.toList()))
                        currentCourses.clear()
                    }
                    currentTerm = termMatch.value
                }

                // Match course details
                val courseMatch = coursePattern.find(line)
                if (courseMatch != null && currentTerm != null) {
                    val (courseCode, courseName, attempted, earned, grade, points) = courseMatch.destructured
                    val course = Course(
                        term = currentTerm,
                        courseCode = courseCode.trim().replace("\\s+".toRegex(), " "),
                        courseName = courseName.trim().replace("\\s+".toRegex(), " "),
                        attempted = attempted.toDouble().toInt(),
                        earned = earned.toDouble().toInt(),
                        points = points.toDouble(),
                        grade = grade.trim()
                    )
                    currentCourses.add(course)
                }
            }

            // Add the last term after finishing the loop
            if (currentTerm != null && currentCourses.isNotEmpty()) {
                terms.add(Term(name = currentTerm, courses = currentCourses))
            }

            return Transcript(terms = terms)
        }


    }


}