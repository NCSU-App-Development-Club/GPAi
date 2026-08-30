package org.appdevncsu.gpai.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import org.appdevncsu.gpai.api.AuthorizationInterceptor
import org.appdevncsu.gpai.api.models.SignInRequest
import org.appdevncsu.gpai.api.repositories.Repository
import org.appdevncsu.gpai.models.User
import org.appdevncsu.gpai.security.CredentialsStore
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject

class AuthViewModel(
    private val credentialsStore: CredentialsStore,
    private val interceptor: AuthorizationInterceptor,
) : ViewModel(), KoinComponent {

    private val _loading: MutableStateFlow<Boolean> = MutableStateFlow(true)
    val loading = _loading.asStateFlow()

    private val _error: MutableStateFlow<Boolean> = MutableStateFlow(false)
    val error = _error.asStateFlow()

    private val _user: MutableStateFlow<User?> = MutableStateFlow(null)
    val user = _user.asStateFlow()

    private val _clientId: MutableStateFlow<String?> = MutableStateFlow(null)
    val clientId = _clientId.asStateFlow()

    private val _deleteAccountEvent = MutableSharedFlow<Boolean>()
    val deleteAccountEvent = _deleteAccountEvent.asSharedFlow()

    private val repository: Repository by inject()

    init {
        load()
    }

    /**
     * Loads the persisted user and fetches the backend config.
     */
    fun load() {
        _loading.value = true
        _error.value = false

        val getUserJob = viewModelScope.async {
            val user = try {
                credentialsStore.user()
            } catch (e: Exception) {
                _error.value = true
                e.printStackTrace()
                null
            }
            _user.value = user
            if (!user?.token.isNullOrEmpty()) {
                interceptor.setToken(user.token)
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
                credentialsStore.save(user)
                interceptor.setToken(user.token)
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
        val photoURL = credential.profilePictureUri?.toString()

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

    fun setError(error: Boolean) {
        _error.value = error
    }

    fun signOut() {
        _user.value = null
        viewModelScope.launch {
            repository.signOut()
            interceptor.clearToken()
            credentialsStore.clear()
        }
    }

    fun deleteAccount() {
        viewModelScope.launch {
            val result = repository.deleteAccount()
            if (result.isSuccess) {
                _user.value = null
                interceptor.clearToken()
                credentialsStore.clear()
                _deleteAccountEvent.emit(true)
            } else {
                result.exceptionOrNull()?.printStackTrace()
                _deleteAccountEvent.emit(false)
            }
        }
    }
}
