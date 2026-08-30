package org.appdevncsu.gpai.util

import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

class AnalyticsHelper(private val analytics: FirebaseAnalytics) {

    fun logSignIn() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "google")
        }
        analytics.logEvent(FirebaseAnalytics.Event.LOGIN, bundle)
    }

    fun logSignOut() {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.METHOD, "google")
        }
        analytics.logEvent("sign_out", bundle)
    }

    fun logChatMessageSent() {
        analytics.logEvent("chat_message_sent", Bundle())
    }

    fun logTranscriptUploaded() {
        analytics.logEvent("transcript_uploaded", Bundle())
    }

    fun logMessageFlagged(reason: String) {
        val bundle = Bundle().apply {
            putString("reason", reason)
        }
        analytics.logEvent("message_flagged", bundle)
    }

    fun logScreenView(screenName: String) {
        val bundle = Bundle().apply {
            putString(FirebaseAnalytics.Param.SCREEN_NAME, screenName)
            putString(FirebaseAnalytics.Param.SCREEN_CLASS, screenName)
        }
        analytics.logEvent(FirebaseAnalytics.Event.SCREEN_VIEW, bundle)
    }
}
