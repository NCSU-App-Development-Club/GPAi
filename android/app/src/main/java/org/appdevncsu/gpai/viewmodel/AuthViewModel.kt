package org.appdevncsu.gpai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.appdevncsu.gpai.api.AuthorizationInterceptor
import org.appdevncsu.gpai.api.models.SignInRequest
import org.appdevncsu.gpai.api.repositories.Repository
import org.appdevncsu.gpai.api.repositories.RepositoryImpl
import org.appdevncsu.gpai.models.User
import org.appdevncsu.gpai.models.UserDTO
import org.appdevncsu.gpai.room.AppDatabase
import org.koin.java.KoinJavaComponent
import kotlin.getValue

class AuthViewModel(private val db: AppDatabase) : ViewModel() {

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _error: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error = _error.asStateFlow()

    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    val user = _user.asStateFlow()

    private val _clientId: MutableStateFlow<String?> = MutableStateFlow(null)
    val clientId = _clientId.asStateFlow()

    private val repository: Repository by KoinJavaComponent.inject(RepositoryImpl::class.java)

    init {
        val getUserJob = viewModelScope.async {
            val user = try {
                db.userDao().getUser()?.toUser()
            } catch (e: Exception) {
                _error.value = true
                e.printStackTrace()
                null
            }
            _user.value = user
            if (user != null) {
                AuthorizationInterceptor.setToken(user.token)
            }
        }

        val getConfigJob = viewModelScope.async {
            val config = repository.getConfig()
            if (config.isSuccess) {
                _clientId.value = config.getOrThrow().clientId
            } else {
                config.exceptionOrNull()?.printStackTrace()
                _error.value = true
            }
        }

        viewModelScope.launch {
            getUserJob.join()
            getConfigJob.join()
            _loading.update { false }
        }
    }

    fun setCurrentUser(user: User) {
        val prev = _user.value
        _user.update { user }
        viewModelScope.launch {
            try {
                db.userDao().setUser(UserDTO.from(user))
            } catch (e: Exception) {
                e.printStackTrace()
                // Revert the change because the update didn't succeed
                _user.update { prev }
            }
        }
    }

    suspend fun handleLoginRequest(credential: GoogleIdTokenCredential) {
        val email = credential.id
        val idToken =
            credential.idToken // The token we need to send to the server to verify the user account
        val name = credential.displayName // The user's full name
        val photoURL = credential.profilePictureUri?.toString().orEmpty()

        val signInResponse = repository.signIn(SignInRequest(idToken))
        if (signInResponse.isSuccess) {
            setCurrentUser(
                User(
                    name ?: email,
                    email,
                    email,
                    photoURL,
                    signInResponse.getOrThrow().sessionID
                )
            )
        } else {
            throw signInResponse.exceptionOrNull()!!
        }
    }

    fun clearError() {
        _error.value = false
    }

    fun setError(error: Boolean) {
        _error.value = error
    }
}
