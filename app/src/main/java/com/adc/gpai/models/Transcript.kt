package com.adc.gpai.models

import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
/**
 * Data class representing a student's transcript, which contains a list of academic terms.
 * Each term includes a list of courses, and the transcript calculates total credits attempted,
 * earned, and grade points for GPA calculations.
 *
 * @param terms A list of academic terms in the transcript, each containing a list of courses.
 */
data class Transcript(val terms: List<Term> = ArrayList()) {

 /**
  * Calculates the total number of credits earned across all terms and courses in the transcript.
  *
  * @return The total credits earned by summing the `earned` value of each course.
  */
 val totalCredits: Int
  get() {
   var earned = 0
   // Loop through each term and course, summing the earned credits
   for (term in terms) {
    for (course in term.courses) {
     earned += course.attempted
    }
   }
   return earned
  }

 /**
  * Calculates the total grade points earned across all terms and courses in the transcript.
  * Grade points are typically used for GPA calculations.
  *
  * @return The total grade points earned by summing the `points` value of each course.
  */
 val totalEarnedPoints: Double
  get() {
   var points = 0.0
   // Loop through each term and course, summing the grade points
   for (term in terms) {
    for (course in term.courses) {
     points += course.points
    }
   }
   return points
  }

 /**
  * The GPA (Grade Point Average) calculated based on the total earned points
  * and the total credits.
  *
  * If `totalCredits` is 0, the GPA will be returned as 0.0 to avoid
  * division by zero.
  *
  * @return the calculated GPA as a [Double]
  */
 val gpa: Double
  get() {
   if (totalCredits == 0) return 0.0
   return totalEarnedPoints / totalCredits
  }

}
