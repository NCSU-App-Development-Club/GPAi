package com.adc.gpai.onboarding

import AppDatabase
import OnboardingViewModelFactory
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.adc.gpai.models.Course
import com.adc.gpai.ui.theme.GPAiTheme

/**
 * OnboardingActivity is a subclass of ComponentActivity that serves as the entry point
 * for the onboarding flow. It sets up the activity with edge-to-edge display and initializes
 * the OnboardingScreen composable.
 */
class OnboardingActivity : ComponentActivity() {
    //TODO: Probably needs to be in a Forecaster viewmodel instead of the onboarding activity

    // ViewModel for managing the state of the onboarding flow.
    //val onboardingViewModel = OnboardingViewModel(database:AppDataBase)
    private lateinit var courseRepository: OnboardingViewModel

    /**
     * Called when the activity is starting. This is where the onboarding screen is set up
     * and displayed, along with enabling edge-to-edge UI for a seamless experience.
     *
     * @param savedInstanceState If the activity is being re-initialized after previously
     * being shut down, this Bundle contains the data it most recently supplied. Otherwise,
     * it is null.
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            OnboardingScreen(viewModel = courseRepository)
        }
        val database = AppDatabase.getDatabase(this)
        courseRepository = ViewModelProvider(this, OnboardingViewModelFactory(database)).get(OnboardingViewModel::class.java)

        //TODO: FIND WHAT YOU ACTUALLY NEED TO ENTER TO DB< BELOW ARE EXAMPLES OF CALLS
        // Example: Adding a new course
        val CSC116 = Course(
            courseCode = "CSC 116",
            courseName = "Introduction to Programming in Java",
            attempted = 3,
            earned = 3,
            points = 97.0,
            grade = "A"
        )
        courseRepository.addCourse(CSC116)

        val MA141 = Course(
            courseCode = "MA 141",
            courseName = "Calculus I",
            attempted = 4,
            earned = 4,
            points = 94.0,
            grade = "A"
        )
        courseRepository.addCourse(MA141)
        //Test to update
        courseRepository.updateCourse(Course(
            courseCode = "MA 141",
            courseName = "Calculus I",
            attempted = 3,
            earned = 3,
            points = 94.0,
            grade = "A"
        ))
        // Example: Updating an existing course name
        courseRepository.updateCourseName("Intro to programming", CSC116.courseName)

        //Example attempt: update grade
        val newGrade = 98
        courseRepository.updateGrade(newGrade, CSC116.courseName)


        //Example attempt: update credit hours attempted and earned
        val newCH = 2
        courseRepository.updateHoursAttempted(newCH, CSC116.courseName)
        courseRepository.updateHoursEarned(newCH, CSC116.courseName)

        //EX attempt, update course code
        courseRepository.updateCourseCode("CSC101", CSC116.courseName)


        //Example attempt: delete a course
        courseRepository.deleteCourse(CSC116)


        // Observe the users LiveData
        courseRepository.courses.observe(this, Observer { userList ->
            // Update UI with the user list

    })


}

/**
 * Composable function representing the OnboardingScreen. This screen is wrapped inside
 * a Scaffold and manages the navigation for the onboarding flow through a navigation graph.
 *
 * @param viewModel The ViewModel that handles the state and logic of the onboarding process.
 */
@Composable
fun OnboardingScreen(viewModel: OnboardingViewModel) {
    GPAiTheme {
        Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
            // Sets up a navigation controller for managing the navigation within the onboarding flow.
            val navController = rememberNavController()
            OnboardingNavGraph(
                navController = navController,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}


/**
 * A composable function that manages the navigation between different screens (Forecaster and Advisor)
 * based on the current state of the home screen.
 *
 * @param navController The navigation controller used to manage navigation between composables.
 * @param modifier Modifier to apply to the NavGraph container.
 */
@Composable
fun OnboardingNavGraph(
    navController: NavHostController, modifier: Modifier = Modifier
) {
    // Determine the start destination based on the current home state
    val startDestination = "intro"

    // Set up the NavHost with composable screens for navigation
    NavHost(
        navController = navController,
        startDestination = startDestination, // Start with the determined destination
        modifier = modifier // Apply the passed modifier (e.g., fillMaxSize)
    ) {
        // Forecaster screen with slide-in/out animations
        composable("intro",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            IntroScreen(navController = navController) // Displays the Intro screen
        }

        // Advisor screen with slide-in/out animations
        composable("upload",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            UploadTranscriptScreen(navController = navController) // Displays the Upload Transcript screen
        }

        // Advisor screen with slide-in/out animations
        composable("modify",
            enterTransition = { slideInHorizontally(initialOffsetX = { 2000 }) + fadeIn() },
            exitTransition = { slideOutHorizontally(targetOffsetX = { -2000 }) + fadeOut() },
            popEnterTransition = { slideInHorizontally(initialOffsetX = { -1000 }) + fadeIn() },
            popExitTransition = { slideOutHorizontally(targetOffsetX = { 1000 }) + fadeOut() }) {
            ModifyTranscriptScreen(navController = navController) // Displays the Modify Transcript screen
        }
    }
}}
