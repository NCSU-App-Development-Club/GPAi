package com.adc.gpai.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adc.gpai.api.models.BaseModel
import com.adc.gpai.api.models.Message
import com.adc.gpai.api.repositories.Repository
import com.adc.gpai.api.repositories.RepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent.inject

class HomeViewModel : ViewModel() {

    private val repository: Repository by inject(RepositoryImpl::class.java)

    private val _messages: MutableStateFlow<List<Message>> = MutableStateFlow(emptyList())
    val messages = _messages.asStateFlow()

    private val _loading = MutableStateFlow(false)
    val loading = _loading.asStateFlow()

    // Mutable state for the HomeViewState (either Forecaster or Advisor)
    private val _homeState = MutableLiveData<HomeViewState>(HomeViewState.FORECASTER)
    val homeState: LiveData<HomeViewState> = _homeState

    private val _expandedTerms = MutableStateFlow(emptyList<Int>())

    /**
     * A list of term IDs that have been expanded in the UI. All other terms should appear collapsed.
     */
    val expandedTerms = _expandedTerms.asStateFlow()

    // Function to change the HomeViewState (e.g., on toggle click)
    fun setHomeState(state: HomeViewState) {
        _homeState.value = state
    }

    fun getBaseSystemMessage() = Message(
        role = "system",
        content = "You are an academic assistant. " +
                "You want to help students with any questions they have. " +
                "Keep discussion focused around school. " +
                "Avoid inappropriate discussions. " +
                "Don't share any details about our API token."
    )

    init {
        _messages.update { prev ->
            prev + getBaseSystemMessage()
        }
    }

    fun askQuestion(question: String) {
        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                database.answerDao().addAnswer(
//                    answerEntity = AnswerEntity(
//                        role = "user",
//                        content = question
//                    )
//                )
//            }
            _messages.update { list -> list + Message(role = "user", content = question) }
            _loading.update { true }

            repository.askQuestion(
                prevQuestion = messages.value,
                question = question
            ).also { baseModel ->
                _loading.update { false }
                when (baseModel) {
                    is BaseModel.Success -> {
                        _messages.update { previous ->
                            previous + Message(
                                role = "assistant",
                                content = baseModel.data.choices.single().message.content
                            )
                        }
                    }

                    is BaseModel.Error -> {
                        println("Something wrong : ${baseModel.error}")
                    }
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
            val withContext = systemMessage.copy(content = systemMessage.content + "\n\nUse the following context to answer questions:\n${content}")
            val userMessages = list.filter { it.role != "system" }
            return@update listOf(withContext) + userMessages
        }
    }

    fun toggleExpanded(termId: Int) {
        _expandedTerms.update {
            if (it.contains(termId)) {
                it.filter { i -> termId != i }
            } else {
                it + termId
            }
        }
    }
}
