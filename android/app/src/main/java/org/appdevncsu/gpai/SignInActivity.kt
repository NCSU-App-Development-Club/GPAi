package org.appdevncsu.gpai

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.sp
import org.appdevncsu.gpai.home.HomeActivity
import org.appdevncsu.gpai.home.SignInScreen
import org.appdevncsu.gpai.onboarding.OnboardingActivity
import org.appdevncsu.gpai.onboarding.TranscriptRepository
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.appdevncsu.gpai.viewmodel.AuthViewModel
import org.koin.androidx.compose.koinViewModel

class SignInActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GPAiTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    SignInOrSignUp(modifier = Modifier.padding(innerPadding))
                }
            }
        }
    }
}

@Composable
fun SignInOrSignUp(modifier: Modifier = Modifier) {
    val authViewModel: AuthViewModel = koinViewModel()
    val transcriptViewModel: TranscriptRepository = koinViewModel()

    val transcriptLoading by transcriptViewModel.loading.collectAsState()
    val transcript by transcriptViewModel.transcript.collectAsState()

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
        } else if (loading || transcriptLoading) {
            CircularProgressIndicator()
        } else if (user == null) {
            SignInScreen()
        } else if (transcript == null) {
            LocalContext.current.startActivity(
                Intent(
                    LocalContext.current,
                    OnboardingActivity::class.java
                )
            )
        } else {
            LocalContext.current.startActivity(
                Intent(
                    LocalContext.current,
                    HomeActivity::class.java
                )
            )
        }
    }
}
