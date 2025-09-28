package org.appdevncsu.gpai.screen.advisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.sp
import org.appdevncsu.gpai.screen.SignInScreen
import org.appdevncsu.gpai.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

@Composable
fun AuthGate(modifier: Modifier = Modifier, child: @Composable () -> Unit) {
    val authViewModel: AuthViewModel = koinViewModel()

    val error by authViewModel.error.collectAsState()
    val loading by authViewModel.loading.collectAsState()
    val user by authViewModel.user.collectAsState()

    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (error) {
            Column(
                modifier = modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("There was a problem signing you in", fontSize = 20.sp)
                Text("Please try again later.", fontSize = 14.sp)
            }
        } else if (loading) {
            CircularProgressIndicator()
        } else if (user == null) {
            SignInScreen()
        } else {
            child()
        }
    }
}
