package org.appdevncsu.gpai.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.GetCredentialException
import androidx.navigation.NavHostController
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
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

    var key by remember { mutableIntStateOf(0) }
    var ncsuDomainRequired by remember { mutableStateOf(false) }

    LaunchedEffect("sign-in-request-$key") {
        try {
            val result = credentialManager.getCredential(
                context = context,
                request = credentialRequest
            )
            handleSignIn(result, authViewModel)
        } catch (e: GetCredentialException) {
            // Handle failure
            e.printStackTrace()
        } catch (_: RepositoryImpl.InvalidDomainException) {
            ncsuDomainRequired = true
        } catch (e: Exception) {
            e.printStackTrace()
            authViewModel.setError(true)
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
                Text("Invalid Account Type", fontSize = 24.sp)
                Text("Sign in with your @ncsu.edu Google account", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                Button(onClick = {
                    // Make the LaunchedEffect run again
                    ncsuDomainRequired = false
                    key++
                }) {
                    Text("Retry")
                }
            } else {
                Text("Sign in to GPAi", fontSize = 24.sp)
                Text("with your NC State Google account", fontSize = 14.sp, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
                Button(onClick = {
                    // Make the LaunchedEffect run again
                    key++
                }) {
                    Text("Sign In")
                }
            }
        }
    }
}

private suspend fun handleSignIn(result: GetCredentialResponse, viewModel: AuthViewModel) {
    val credential = result.credential
    when (credential) {
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