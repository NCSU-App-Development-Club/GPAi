package org.appdevncsu.gpai.screen.advisor

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import org.appdevncsu.gpai.activity.scopedKoinViewModel
import org.appdevncsu.gpai.models.User
import org.appdevncsu.gpai.screen.SignInScreen
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.appdevncsu.gpai.viewmodel.AuthViewModel

@Composable
fun AuthGate(navController: NavHostController, modifier: Modifier = Modifier, child: @Composable () -> Unit) {
    val authViewModel: AuthViewModel = scopedKoinViewModel(navController)

    val error by authViewModel.error.collectAsState()
    val loading by authViewModel.loading.collectAsState()
    val user by authViewModel.user.collectAsState()

    AuthGateContent(
        navController = navController,
        error = error,
        loading = loading,
        user = user,
        onRetry = { authViewModel.load() },
        modifier = modifier,
        child = child
    )
}

@Composable
private fun AuthGateContent(
    navController: NavHostController,
    error: Boolean,
    loading: Boolean,
    user: User?,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier,
    child: @Composable () -> Unit
) {
    Column(
        modifier = modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when {
            error -> {
                Text("There was a problem signing you in", fontSize = 20.sp)
                Text(
                    "Please try again later.",
                    fontSize = 14.sp,
                    modifier = Modifier.padding(top = 4.dp, bottom = 8.dp)
                )
                Button(onClick = onRetry) {
                    Text("Retry")
                }
            }

            loading -> CircularProgressIndicator()
            user == null -> SignInScreen(navController)
            else -> child()
        }
    }
}

@Preview(showBackground = true)
@Composable
fun AuthGateLoadingPreview() {
    GPAiTheme {
        AuthGateContent(
            navController = rememberNavController(),
            error = false,
            loading = true,
            user = null,
            onRetry = {},
            child = { Text("Main app content") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthGateErrorPreview() {
    GPAiTheme {
        AuthGateContent(
            navController = rememberNavController(),
            error = true,
            loading = false,
            user = null,
            onRetry = {},
            child = { Text("Main app content") }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun AuthGateSuccessPreview() {
    GPAiTheme {
        AuthGateContent(
            navController = rememberNavController(),
            error = false,
            loading = false,
            user = User("Test User", "test@ncsu.edu", "test@ncsu.edu", "", "token"),
            onRetry = {},
            child = { Text("Main app content") }
        )
    }
}
