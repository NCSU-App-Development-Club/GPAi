package org.appdevncsu.gpai.viewmodel

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
        viewModelScope.launch {
            _messages.update { list -> list + Message(role = "user", content = question) }
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
                    throw response.exceptionOrNull()!!
                }
            }
        }
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
