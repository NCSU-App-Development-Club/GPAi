package org.appdevncsu.gpai.screen

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.NoCredentialException
import androidx.navigation.NavHostController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import kotlinx.coroutines.launch
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.activity.scopedKoinViewModel
import org.appdevncsu.gpai.api.repositories.RepositoryImpl
import org.appdevncsu.gpai.viewmodel.AuthViewModel

@Composable
fun SignInScreen(navController: NavHostController, modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = scopedKoinViewModel(navController)

    val clientId = authViewModel.clientId.collectAsState()

    if (clientId.value == null) {
        CircularProgressIndicator()
        return
    }

    val googleIdOption: GetGoogleIdOption = GetGoogleIdOption.Builder()
        .setFilterByAuthorizedAccounts(false)
        .setServerClientId(clientId.value!!)
        .setAutoSelectEnabled(true)
        .build()
    val credentialRequest = GetCredentialRequest(listOf(googleIdOption))

    val context = LocalContext.current

    val credentialManager = CredentialManager.create(context)

    var ncsuDomainRequired by remember { mutableStateOf(false) }
    var noCredentials by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun requestCredential() {
        coroutineScope.launch {
            try {
                val result = credentialManager.getCredential(
                    context = context,
                    request = credentialRequest
                )
                handleSignIn(result, authViewModel)
            } catch (_: NoCredentialException) {
                noCredentials = true
            } catch (_: GetCredentialCancellationException) {
                // User cancelled the sign-in prompt; no action needed
            } catch (e: GetCredentialException) {
                e.printStackTrace()
                authViewModel.setError(true)
            } catch (_: RepositoryImpl.InvalidDomainException) {
                ncsuDomainRequired = true
            } catch (e: Exception) {
                Log.e("SignInScreen", "Failed to sign in user", e)
                authViewModel.setError(true)
            }
        }
    }

    Column(
        modifier = modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Column(modifier = Modifier.weight(0.3f)) { }
        Column(
            modifier = Modifier.weight(0.7f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (ncsuDomainRequired) {
                Text(stringResource(R.string.invalid_account_type), fontSize = 24.sp)
                Text(
                    stringResource(R.string.invalid_account_hint),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Button(onClick = {
                    ncsuDomainRequired = false
                    requestCredential()
                }) {
                    Text(stringResource(R.string.retry))
                }
            } else if (noCredentials) {
                Text(stringResource(R.string.no_account_found), fontSize = 24.sp)
                Text(
                    stringResource(R.string.no_account_hint),
                    fontSize = 14.sp,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Button(onClick = {
                    noCredentials = false
                    requestCredential()
                }) {
                    Text(stringResource(R.string.retry))
                }
            } else {
                Text(stringResource(R.string.sign_in_title), fontSize = 24.sp)
                Text(
                    stringResource(R.string.sign_in_subtitle),
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Button(onClick = {
                    requestCredential()
                }) {
                    Text(stringResource(R.string.sign_in))
                }
            }
        }
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, viewModel: AuthViewModel) {
    when (val credential = result.credential) {
        is CustomCredential -> {
            if (credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
                try {
                    val googleIdTokenCredential =
                        GoogleIdTokenCredential.createFrom(credential.data)
                    viewModel.handleLoginRequest(googleIdTokenCredential)
                } catch (e: GoogleIdTokenParsingException) {
                    throw e
                }
            } else {
                error("Unexpected credential type")
            }
        }

        else -> error("Unexpected credential type")
    }
}