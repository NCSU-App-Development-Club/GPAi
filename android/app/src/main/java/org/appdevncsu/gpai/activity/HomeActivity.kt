package org.appdevncsu.gpai.activity

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navigation
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.screen.ForecasterScreen
import org.appdevncsu.gpai.screen.GPAiAppBar
import org.appdevncsu.gpai.screen.HomeViewToggle
import org.appdevncsu.gpai.screen.ProfileScreen
import org.appdevncsu.gpai.screen.advisor.AdvisorScreen
import org.appdevncsu.gpai.screen.onboarding.IntroScreen
import org.appdevncsu.gpai.screen.onboarding.UploadTranscriptScreen
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.appdevncsu.gpai.util.LocalSnackbarRunner
import org.appdevncsu.gpai.util.SnackbarRunner
import org.appdevncsu.gpai.viewmodel.AuthViewModel
import org.appdevncsu.gpai.viewmodel.TranscriptRepository
import org.koin.androidx.compose.koinViewModel

/**
 * Main activity of the application that sets up the layout and navigation using Jetpack Compose.
 */
class HomeActivity : ComponentActivity() {

    /**
     * Called when the activity is starting. Sets up the UI, navigation controller, and
     * manages the state of the home screen.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously being shut down,
     *                           this Bundle contains the data it most recently supplied.
     */
    @OptIn(ExperimentalAnimationApi::class, ExperimentalMaterial3Api::class)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge() // Enables edge-to-edge mode for better UI experience on modern devices
        setContent {
            GPAiTheme {
                val navController = rememberNavController()
                val scope = rememberCoroutineScope()
                val snackbarRunner = remember { SnackbarRunner(scope) }

                val currentBackStackEntry =
                    navController.currentBackStackEntryFlow.collectAsState(null)
                val currentRoute = currentBackStackEntry.value?.destination?.route
                val isHomeScreen = currentRoute == "forecaster" || currentRoute == "advisor"

                CompositionLocalProvider(LocalSnackbarRunner provides snackbarRunner) {
                    Scaffold(
                        modifier = Modifier.fillMaxSize(),
                        snackbarHost = { SnackbarHost(snackbarRunner.hostState) },
                        topBar = {
                            if (isHomeScreen) {
                                val authViewModel: AuthViewModel =
                                    scopedKoinViewModel(navController)
                                val user by authViewModel.user.collectAsState()
                                GPAiAppBar(navController, photoURL = user?.photoURL)
                            } else if (currentRoute == "profile") {
                                TopAppBar(
                                    title = {
                                        Text(
                                            text = stringResource(R.string.profile),
                                            fontWeight = FontWeight.Black,
                                            fontSize = 24.sp
                                        )
                                    },
                                    navigationIcon = {
                                        IconButton(onClick = { navController.popBackStack() }) {
                                            Icon(
                                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                                contentDescription = stringResource(R.string.back),
                                            )
                                        }
                                    },
                                    modifier = Modifier.height(104.dp)
                                )
                            }
                        }
                    ) { innerPadding ->
                        val authViewModel: AuthViewModel = scopedKoinViewModel(navController)
                        val deleteSuccessMessage = stringResource(R.string.delete_account_success)
                        val deleteFailureMessage =
                            stringResource(R.string.delete_account_error_message)
                        LaunchedEffect(Unit) {
                            authViewModel.deleteAccountEvent.collect { success ->
                                val message =
                                    if (success) deleteSuccessMessage else deleteFailureMessage
                                snackbarRunner.send(message)
                            }
                        }
                        Column(modifier = Modifier.padding(innerPadding)) {
                            AppContainer(
                                navController,
                                modifier = Modifier.weight(if (isHomeScreen) 0.9f else 1.0f)
                            )

                            if (isHomeScreen) {
                                // Toggle button at the bottom of the screen to switch between views
                                HomeViewToggle(
                                    currentRoute = currentRoute,
                                    navController = navController,
                                    modifier = Modifier
                                        .fillMaxWidth() // Takes full width
                                        .weight(0.1f) // Takes up 10% of the screen height
                                )

                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AppContainer(navController: NavHostController, modifier: Modifier = Modifier) {

    val transcriptViewModel: TranscriptRepository = koinViewModel()
    val loading = transcriptViewModel.loading.collectAsState()
    if (loading.value) {
        CircularProgressIndicator()
        return
    }

    val transcript = transcriptViewModel.transcript.collectAsState()

    val startDestination = if ((transcript.value?.terms?.size ?: 0) > 0) "home_graph" else "intro"

    NavHost(navController, startDestination = startDestination, modifier) {
        // Forecaster screen with slide-in/out animations
        composable(
            "intro",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            IntroScreen(navController = navController) // Displays the Intro screen
        }

        // Advisor screen with slide-in/out animations
        composable(
            "upload",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            UploadTranscriptScreen(navController = navController) // Displays the Upload Transcript screen
        }

        navigation(startDestination = "forecaster", route = "home_graph") {
            composable(
                "forecaster",
                enterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() }) {
                ForecasterScreen(navController)
            }

            composable(
                "advisor",
                enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                exitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() },
                popEnterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
                popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }) {
                AdvisorScreen(navController)
            }
        }

        composable(
            "profile",
            enterTransition = { slideInHorizontally(initialOffsetX = { it }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -it }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { it }) + fadeOut() }) {
            val authViewModel: AuthViewModel = scopedKoinViewModel(navController)
            val user by authViewModel.user.collectAsState()
            ProfileScreen(
                navController = navController,
                userName = user?.name.orEmpty(),
                userEmail = user?.email.orEmpty(),
                photoURL = user?.photoURL,
                isSignedIn = user != null,
                onSignOut = { authViewModel.signOut() },
                onDeleteAccount = { authViewModel.deleteAccount() },
            )
        }
    }
}

/**
 * Returns an instance of the [T] that is scoped to the `home_graph` navigation graph.
 * When transitioning between the forecaster and advisor screens, this instance of the ViewModel
 * is not cleaned up because it's scoped to the parent graph, not each individual destination.
 */
@Composable
inline fun <reified T : ViewModel> scopedViewModel(navController: NavHostController): T {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry("home_graph")
    }
    return viewModel<T>(viewModelStoreOwner = parentEntry)
}

/**
 * Returns an instance of the [T] that is scoped to the `home_graph` navigation graph.
 * When transitioning between the forecaster and advisor screens, this instance of the ViewModel
 * is not cleaned up because it's scoped to the parent graph, not each individual destination.
 */
@Composable
inline fun <reified T : ViewModel> scopedKoinViewModel(navController: NavHostController): T {
    val parentEntry = remember(navController.currentBackStackEntry) {
        navController.getBackStackEntry("home_graph")
    }
    return koinViewModel<T>(viewModelStoreOwner = parentEntry)
}
