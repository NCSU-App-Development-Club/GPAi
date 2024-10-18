package com.adc.gpai.home

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.haw.takonappcompose.models.BaseModel
import com.haw.takonappcompose.models.Message
import com.haw.takonappcompose.repositories.Repository
import com.haw.takonappcompose.repositories.RepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    // Function to change the HomeViewState (e.g., on toggle click)
    fun setHomeState(state: HomeViewState) {
        _homeState.value = state
    }
    init {
        _messages.update { prev -> prev + Message(
            role = "system",
            content = "You are an academic assistant. " +
                    "You want to  help students with any questions they have." +
                    "Keep discussion focused around school." +
                    "Avoid inappropriate discussions" +
                    "Dont share any details about our API token."
        ) }
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
            _loading.update { true }
            Log.d("Swanson", "askQuestion: $question")
            repository.askQuestion(
                prevQuestion = messages.value,
                question = question
            ).also { baseModel ->
                _loading.update { false }
                when (baseModel) {
                    is BaseModel.Success -> {
                        _messages.update { previous -> previous + Message(
                            role = "assistant",
                            content = baseModel.data.choices.first().message.content
                        ) }
                        Log.d("Swanson", baseModel.data.choices.first().message.content)
                    }
                    is  BaseModel.Error -> {
                        Log.d("Swanson", baseModel.error)
                        println("Something wrong : ${baseModel.error}")
                    }

                    else -> {
                        Log.d("Swanson", "Something else")
                    }
                }
            }
        }
    }

}
