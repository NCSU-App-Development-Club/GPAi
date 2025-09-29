package org.appdevncsu.gpai

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.appdevncsu.gpai.activity.HomeActivity
import org.appdevncsu.gpai.onboarding.OnboardingActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This class contains UI tests for the onboarding activity in the app.
 * It uses AndroidJUnit4 to run the tests and the Android Compose testing framework
 * to simulate user interactions with the UI elements.
 */
@RunWith(AndroidJUnit4::class)
class OnboardingActivityTest {

    /**
     * Rule that provides access to the Android Compose testing framework for the
     * OnboardingActivity.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<OnboardingActivity>()

    /**
     * Tests the complete flow of the onboarding screens by simulating user interactions.
     * The test clicks through the "Next" buttons of different screens and verifies that
     * the correct screens are displayed in sequence. Finally, it checks if the HomeActivity
     * is launched after the onboarding is completed.
     */
    @Test
    fun testOnboardingScreenFlow() {
        Intents.init()

        // Verify and interact with the Intro screen
        composeTestRule.onNodeWithTag("intro_screen")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Get Started")
            .performClick()

        // Verify and interact with the Upload screen
        composeTestRule.onNodeWithTag("upload_screen")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Next")
            .performClick()

        // Verify and interact with the Modify screen
        composeTestRule.onNodeWithTag("modify_screen")
            .assertIsDisplayed()
        composeTestRule.onNodeWithText("Finish")
            .performClick()

        // Verify if HomeActivity is launched
        Intents.intended(IntentMatchers.hasComponent(HomeActivity::class.java.name))
        Intents.release()
    }
}
