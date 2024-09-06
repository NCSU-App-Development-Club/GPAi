package com.adc.gpai.models

import kotlinx.serialization.Serializable

@Serializable
 data class Transcript (val terms: List<Term> = ArrayList<Term>()) {
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

  val totalAttempted: Int
   get() {
    var attempted = 0
    for(term in terms) {
     for (course in term.courses) {
      attempted += course.attempted
     }
    }
    return attempted
   }

  val totalEarned: Int
   get() {
    var earned = 0
    for(term in terms) {
     for (course in term.courses) {
      earned += course.earned
     }
    }
    return earned
   }

  val totalPoints: Double
   get() {
    var points = 0.0
    for(term in terms) {
     for (course in term.courses) {
      points += course.points
     }
    }
    return points
   }
 }