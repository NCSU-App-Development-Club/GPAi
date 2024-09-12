package com.adc.gpai.models

import kotlinx.serialization.Serializable

@Serializable
/**
 * Data class representing a student's transcript, which contains a list of academic terms.
 * Each term includes a list of courses, and the transcript calculates total credits attempted,
 * earned, and grade points for GPA calculations.
 *
 * @param terms A list of academic terms in the transcript, each containing a list of courses.
 */
data class Transcript(val terms: List<Term> = ArrayList<Term>()) {

 // Constants representing grade points for various grades
 val A_PLUS = 4.333
 val A = 4.0
 val A_MINUS = 3.667
 val B_PLUS = 3.333
 val B = 3.0
 val B_MINUS = 2.667
 val C_PLUS = 2.333
 val C = 2.0
 val C_MINUS = 1.667
 val D_PLUS = 1.333
 val D = 1.0
 val D_MINUS = 0.667
 val F = 0.0

 /**
  * Calculates the total number of credits attempted across all terms and courses in the transcript.
  *
  * @return The total credits attempted by summing the `attempted` value of each course.
  */
 val totalAttempted: Int
  get() {
   var attempted = 0
   // Loop through each term and course, summing the attempted credits
   for (term in terms) {
    for (course in term.courses) {
     attempted += course.attempted
    }
   }
   return attempted
  }

 /**
  * Calculates the total number of credits earned across all terms and courses in the transcript.
  *
  * @return The total credits earned by summing the `earned` value of each course.
  */
 val totalEarned: Int
  get() {
   var earned = 0
   // Loop through each term and course, summing the earned credits
   for (term in terms) {
    for (course in term.courses) {
     earned += course.earned
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
 val totalPoints: Double
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
}
