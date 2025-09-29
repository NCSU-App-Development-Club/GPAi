package org.appdevncsu.gpai

import androidx.compose.ui.test.*
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.matcher.IntentMatchers
import org.appdevncsu.gpai.activity.HomeActivity
import org.appdevncsu.gpai.onboarding.OnboardingActivity
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * This class contains UI tests for the HomeActivity in the app.
 * It tests the functionality of toggling between the Forecaster and Advisor views,
 * and verifies the behavior of the app bar refresh button.
 */
@RunWith(AndroidJUnit4::class)
class HomeActivityTest {

    /**
     * Rule that provides access to the Android Compose testing framework for the HomeActivity.
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<HomeActivity>()

    /**
     * Tests the functionality of toggling between the Forecaster and Advisor screens.
     * The test checks if the Forecaster screen is displayed initially, then toggles to
     * the Advisor screen and verifies the display, and finally toggles back to the Forecaster screen.
     */
    @Test
    fun homeScreen_toggleToAdvisorView() {
        // Initially, the Forecaster screen should be displayed (check for the unique tag)
        composeTestRule.onNodeWithTag("forecaster_screen")
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("advisor_screen")
            .assertDoesNotExist()

        // Click on the "Advisor" button using the tag
        composeTestRule.onNodeWithTag("advisor").performClick()

        // Verify that the Advisor screen is now displayed (check for the unique tag)
        composeTestRule.onNodeWithTag("advisor_screen")
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("forecaster_screen")
            .assertDoesNotExist()

        // Click on the "Forecaster" button using the tag
        composeTestRule.onNodeWithTag("forecaster").performClick()

        // The Forecaster screen should be displayed (check for the unique tag)
        composeTestRule.onNodeWithTag("forecaster_screen")
            .assertExists()
            .assertIsDisplayed()
        composeTestRule.onNodeWithTag("advisor_screen")
            .assertDoesNotExist()
    }

    /**
     * Tests that clicking the refresh button in the app bar triggers the intent to launch the
     * OnboardingActivity.
     * The test simulates a click on the refresh button and verifies that the correct intent
     * for launching OnboardingActivity is fired.
     */
    @Test
    fun appBar_refreshButtonTriggersOnboardingActivity() {
        Intents.init()
        // Click the refresh button in the app bar
        composeTestRule.onNodeWithContentDescription("Refresh")
            .performClick()

        // Verify that the intent to launch OnboardingActivity was fired
        Intents.intended(IntentMatchers.hasComponent(OnboardingActivity::class.java.name))
        Intents.release()
    }
}
