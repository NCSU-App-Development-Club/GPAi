package org.appdevncsu.gpai.security

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class PreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    var hasAcceptedTerms: Boolean
        get() = prefs.getBoolean(KEY_TERMS_ACCEPTED, false)
        set(value) = prefs.edit { putBoolean(KEY_TERMS_ACCEPTED, value) }

    companion object {
        private const val PREFS_NAME = "gpai_preferences"
        private const val KEY_TERMS_ACCEPTED = "terms_accepted"
    }
}
