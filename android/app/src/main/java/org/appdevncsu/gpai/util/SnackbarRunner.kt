package org.appdevncsu.gpai.util

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.compositionLocalOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

val LocalSnackbarRunner = compositionLocalOf<SnackbarRunner> {
    error("No SnackbarRunner provided")
}

class SnackbarRunner(private val scope: CoroutineScope) {
    val hostState = SnackbarHostState()

    fun send(message: String, duration: SnackbarDuration = SnackbarDuration.Short) {
        scope.launch {
            hostState.showSnackbar(message, duration = duration)
        }
    }
}
