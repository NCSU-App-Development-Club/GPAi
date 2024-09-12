package com.adc.gpai.home

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adc.gpai.ui.theme.BrandDarkPurple
import com.adc.gpai.ui.theme.BrandPurple
import com.adc.gpai.ui.theme.GPAiTheme
import com.google.accompanist.navigation.animation.rememberAnimatedNavController

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
            // Sets the theme for the app's UI
            GPAiTheme {
                Scaffold(
                    modifier = Modifier.fillMaxSize(),
                    topBar = {
                        // Defines the top app bar with a centered title
                        TopAppBar(
                            title = {
                                Text(
                                    text = "GPAi",
                                    fontWeight = FontWeight.Black,
                                    fontSize = 40.sp,
                                    textAlign = TextAlign.Center
                                )
                            },
                            modifier = Modifier.padding(8.dp)
                        )
                    }
                ) { innerPadding ->
                    // Manages the state of the current home view (Forecaster or Advisor)
                    var homeState = remember { mutableStateOf(HomeViewState.FORECASTER) }

                    // Create a NavController for navigating between screens with animations
                    val navController = rememberAnimatedNavController()

                    // Layout for the home screen content
                    Column(
                        modifier = Modifier
                            .fillMaxSize() // Ensures the column takes up the whole screen
                            .padding(innerPadding) // Adjusts for inner padding of the scaffold
                    ) {
                        // Navigation graph for handling screen transitions based on homeState
                        NavGraph(
                            navController = navController,
                            homeState = homeState.value,
                            modifier = Modifier
                                .weight(0.9f) // Takes up 90% of the screen height
                                .fillMaxSize()
                        )
                        Spacer(modifier = Modifier.padding(bottom = 16.dp)) // Adds space below the NavGraph

                        // Toggle button at the bottom of the screen to switch between views
                        HomeViewToggle(
                            mutableState = homeState,
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

/**
 * A composable function that manages the navigation between different screens (Forecaster and Advisor)
 * based on the current state of the home screen.
 *
 * @param navController The navigation controller used to manage navigation between composables.
 * @param homeState The current state of the home view, which determines the start destination.
 * @param modifier Modifier to apply to the NavGraph container.
 */
@Composable
fun NavGraph(
    navController: NavHostController, homeState: HomeViewState, modifier: Modifier = Modifier
) {
    // Determine the start destination based on the current home state
    val startDestination = if (homeState == HomeViewState.FORECASTER) "forecaster" else "advisor"

    // Navigate to the appropriate screen when the home state changes
    LaunchedEffect(homeState) {
        navController.navigate(startDestination) {
            // Clear the back stack to prevent duplicate screens when navigating
            popUpTo(navController.graph.startDestinationId) { inclusive = true }
        }
    }

    // Set up the NavHost with composable screens for navigation
    NavHost(
        navController = navController,
        startDestination = startDestination, // Start with the determined destination
        modifier = modifier // Apply the passed modifier (e.g., fillMaxSize)
    ) {
        // Forecaster screen with slide-in/out animations
        composable("forecaster",
            enterTransition = { slideInHorizontally(initialOffsetX = { -2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { 2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            ForecasterScreen() // Displays the Forecaster screen
        }

        // Advisor screen with slide-in/out animations
        composable("advisor",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            AdvisorScreen() // Displays the Advisor screen
        }
    }
}

/**
 * A composable function for the toggle button at the bottom of the screen that allows switching
 * between the Forecaster and Advisor views.
 *
 * @param mutableState The current state of the home view, which determines the toggle's position.
 * @param navController The navigation controller used to navigate between composables based on the toggle.
 * @param modifier Modifier to apply to the toggle button container.
 */
@Composable
fun HomeViewToggle(
    mutableState: MutableState<HomeViewState>,
    navController: NavController, // Pass NavController here to navigate on toggle
    modifier: Modifier = Modifier
) {
    // Animation transition between Forecaster and Advisor states
    val transition = updateTransition(mutableState.value, label = "state transition")

    // Animate the X offset of the toggle button based on the selected state
    val offsetX by transition.animateDp(label = "offset animation") { state ->
        when (state) {
            HomeViewState.FORECASTER -> 0.dp
            HomeViewState.ADVISOR -> (LocalConfiguration.current.screenWidthDp.dp / 2) - 16.dp
        }
    }

    // Common modifier for toggle buttons
    val commonModifier = Modifier.fillMaxSize()

    // Calculate the width of each toggle button based on screen width
    val boxWidth = LocalConfiguration.current.screenWidthDp.dp / 2 - 16.dp

    // Main container for the toggle, with rounded corners and background color
    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BrandDarkPurple)
            .height(75.dp)
    ) {
        // Movable box that represents the active state (Forecaster or Advisor)
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = offsetX) // Position based on the animated offset
                .clip(RoundedCornerShape(12.dp))
                .background(BrandPurple)
                .fillMaxHeight()
                .width(boxWidth) // Width based on screen size
        ) {}

        // Row containing the toggle buttons for switching between states
        Row(modifier = Modifier.fillMaxSize()) {
            // Toggle button for the Forecaster view
            ToggleButton(
                text = "Forecaster",
                commonModifier.weight(1f),
                mutableState,
                HomeViewState.FORECASTER,
                navController
            )
            // Toggle button for the Advisor view
            ToggleButton(
                text = "Advisor",
                commonModifier.weight(1f),
                mutableState,
                HomeViewState.ADVISOR,
                navController
            )
        }
    }
}

/**
 * A composable function representing a toggle button that switches between different views
 * (Forecaster or Advisor) and updates the navigation state.
 *
 * @param text The text label of the button.
 * @param modifier Modifier to apply to the toggle button.
 * @param mutableState The current state of the home view.
 * @param targetState The state that this button will activate when clicked.
 * @param navController NavController to handle navigation when the button is clicked.
 */
@Composable
fun ToggleButton(
    text: String,
    modifier: Modifier,
    mutableState: MutableState<HomeViewState>,
    targetState: HomeViewState,
    navController: NavController // Use NavController to navigate on click
) {
    // Button container with clickable modifier to switch state
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable {
                mutableState.value = targetState // Update the home state
                // Navigate to the selected screen (Forecaster or Advisor)
                when (targetState) {
                    HomeViewState.FORECASTER -> navController.navigate("forecaster")
                    HomeViewState.ADVISOR -> navController.navigate("advisor")
                }
            }
    ) {
        // Text label inside the toggle button
        Text(
            text,
            color = Color.White,
            fontWeight = if (mutableState.value == targetState) FontWeight.Black else FontWeight.Normal,
            fontSize = 20.sp
        )
    }
}

/**
 * Enum class representing the possible states of the home view (Forecaster or Advisor).
 */
enum class HomeViewState {
    FORECASTER, ADVISOR
}

/**
 * A preview function for displaying the toggle button and navigation content in Android Studio's preview.
 */
@Preview(showBackground = true)
@Composable
fun GreetingPreview2() {
    GPAiTheme {
        // Mock state for the preview
        var homeState = remember { mutableStateOf(HomeViewState.FORECASTER) }
        val navController = rememberNavController()

        // Column layout for the preview
        Column {
            // Display the NavGraph and HomeViewToggle for preview purposes
            NavGraph(
                navController = navController,
                homeState = homeState.value,
                modifier = Modifier.weight(0.1f) // Takes 10% of the preview screen height
            )
            HomeViewToggle(mutableState = homeState, navController = navController)
        }
    }
}
