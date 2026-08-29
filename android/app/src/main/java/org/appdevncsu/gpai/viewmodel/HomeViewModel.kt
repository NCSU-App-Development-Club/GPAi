package org.appdevncsu.gpai.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.api.repositories.Repository
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class HomeViewModel : ViewModel(), KoinComponent {

    private val repository: Repository by inject()
    private val chatRepository: ChatRepository by inject()

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error = _error.asStateFlow()

    private val _pendingRetry = MutableStateFlow<String?>(null)
    val pendingRetry = _pendingRetry.asStateFlow()

    private val _expandedTerms = MutableStateFlow(emptySet<Int>())

    /**
     * A list of term IDs that have been expanded in the UI. All other terms should appear collapsed.
     */
    val expandedTerms = _expandedTerms.asStateFlow()

    init {
        viewModelScope.launch {
            try {
                val saved = chatRepository.loadMessages()
                if (saved.isNotEmpty()) {
                    _messages.value = saved
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun askQuestion(question: String) {
        _error.update { null }
        viewModelScope.launch {
            val userMessage = Message(role = "user", content = question)
            _messages.update { list -> list + userMessage }
            _loading.update { true }

            repository.askQuestion(
                messages = messages.value,
            ).also { response ->
                _loading.update { false }
                if (response.isSuccess) {
                    val answer = response.getOrThrow()
                    _messages.update { previous ->
                        previous + Message(
                            role = "assistant",
                            content = answer.message,
                            signature = answer.signature
                        )
                    }
                } else {
                    Log.e("HomeViewModel", "askQuestion failed for question: $question", response.exceptionOrNull())
                    _messages.update { list -> list.filter { it.id != userMessage.id } }
                    _error.update { "Something went wrong while getting a response. Please try again." }
                    _pendingRetry.update { question }
                }
                persistMessages()
            }
        }
    }

    /**
     * Re-sends the question from the most recent failed request. Re-adds the user
     * message to the chat and retries the backend call.
     */
    fun retry() {
        val question = _pendingRetry.value ?: return
        _pendingRetry.update { null }
        askQuestion(question)
    }

    /**
     * Stores the user's transcript/context as a single `user` message (not a `system` message —
     * the system prompt is owned by the backend). The message is marked [Message.isContext] so the
     * UI hides it, and is kept at the front of the conversation. Replaces any previous context.
     */
    fun setContext(content: String) {
        val contextMessage = Message(
            role = "user",
            content = "Use the following context to answer questions:\n${content}",
            isContext = true
        )
        _messages.update { list ->
            // There is at most one context message; drop any existing one and prepend the new one.
            val withoutOldContext = list.filter { !it.isContext }
            listOf(contextMessage) + withoutOldContext
        }
        persistMessages()
    }

    fun clearMessages() {
        _messages.update { emptyList() }
        _error.update { null }
        _pendingRetry.update { null }
        persistMessages()
    }

    fun expand(termId: Int) {
        _expandedTerms.update { it + termId }
    }

    fun toggleExpanded(termId: Int) {
        _expandedTerms.update {
            if (it.contains(termId)) {
                it - termId
            } else {
                it + termId
            }
        }
    }

    private fun persistMessages() {
        viewModelScope.launch {
            try {
                chatRepository.saveMessages(_messages.value)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}
