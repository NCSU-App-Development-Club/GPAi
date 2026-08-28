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
import org.appdevncsu.gpai.api.repositories.RepositoryImpl
import org.koin.java.KoinJavaComponent

class HomeViewModel : ViewModel() {

    private val repository: Repository by KoinJavaComponent.inject(RepositoryImpl::class.java)

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

    fun getBaseSystemMessage() = Message(
        role = "system",
        content = "You are an academic assistant. " +
                "You want to help students with any questions they have. " +
                "Keep discussion focused around school. " +
                "Avoid inappropriate discussions."
    )

    init {
        _messages.update { prev ->
            prev + getBaseSystemMessage()
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
                    _messages.update { previous ->
                        previous + Message(
                            role = "assistant",
                            content = response.getOrThrow().message
                        )
                    }
                } else {
                    Log.e("HomeViewModel", "askQuestion failed for question: $question", response.exceptionOrNull())
                    _messages.update { list -> list.filter { it.id != userMessage.id } }
                    _error.update { "Something went wrong while getting a response. Please try again." }
                    _pendingRetry.update { question }
                }
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
     * Updates the conversation's first system message to include the provided [content].
     */
    fun setContext(content: String) {
        _messages.update { list ->
            val systemMessage = getBaseSystemMessage()
            val withContext =
                systemMessage.copy(content = systemMessage.content + "\n\nUse the following context to answer questions:\n${content}")
            val userMessages = list.filter { it.role != "system" }
            return@update listOf(withContext) + userMessages
        }
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
}
