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
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import org.appdevncsu.gpai.screen.ForecasterScreen
import org.appdevncsu.gpai.screen.GPAiAppBar
import org.appdevncsu.gpai.screen.HomeViewToggle
import org.appdevncsu.gpai.screen.advisor.AdvisorScreen
import org.appdevncsu.gpai.screen.onboarding.IntroScreen
import org.appdevncsu.gpai.screen.onboarding.ModifyTranscriptScreen
import org.appdevncsu.gpai.screen.onboarding.UploadTranscriptScreen
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
            val navController = rememberNavController()

            val currentBackStackEntry = navController.currentBackStackEntryFlow.collectAsState(null)
            val currentRoute = currentBackStackEntry.value?.destination?.route
            val isHomeScreen = currentRoute == "forecaster" || currentRoute == "advisor"

            Scaffold(
                modifier = Modifier.fillMaxSize(), topBar = {
                    if (isHomeScreen) {
                        GPAiAppBar(navController)
                    }
                }
            ) { innerPadding ->
                Column(modifier = Modifier.padding(innerPadding)) {
                    AppContainer(
                        navController,
                        modifier = Modifier.weight(if (isHomeScreen) 0.9f else 1.0f)
                    )

                    if (isHomeScreen) {
                        Spacer(modifier = Modifier.padding(bottom = 16.dp)) // Adds space below the NavGraph

                        // Toggle button at the bottom of the screen to switch between views
                        HomeViewToggle(
                            currentRoute = currentRoute,
                            navController = navController,
                            modifier = Modifier
                                .fillMaxWidth() // Takes full width
                                .weight(0.1f) // Takes up 10% of the screen height
                        )

                        Spacer(modifier = Modifier.padding(bottom = 16.dp)) // Adds space below the toggle
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

    val startDestination = if ((transcript.value?.terms?.size ?: 0) > 0) "forecaster" else "intro"

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

        // Advisor screen with slide-in/out animations
        composable(
            "modify",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            ModifyTranscriptScreen(navController) // Displays the Modify Transcript screen
        }

        // Forecaster screen with slide-in/out animations
        composable(
            "forecaster",
            enterTransition = { slideInHorizontally(initialOffsetX = { -2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { 2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            ForecasterScreen() // Displays the Forecaster screen
        }

        // Advisor screen with slide-in/out animations
        composable(
            "advisor",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            AdvisorScreen() // Displays the Advisor screen
        }
    }
}
