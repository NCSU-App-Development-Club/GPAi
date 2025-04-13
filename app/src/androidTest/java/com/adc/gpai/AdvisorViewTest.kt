package com.adc.gpai

import androidx.activity.ComponentActivity
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.adc.gpai.home.AdvisorScreen
import com.adc.gpai.home.HomeViewModel
import org.junit.Test
import org.junit.Rule

/**
 * Class to test the Advisor View screen and make sure it has the right elements which are
 * initialized properly
 *
 * I couldn't figure out how to get the tests to actually run locally, I kept running into errors
 * but I still tried to start laying it out
 */
class UIAdvisorViewTest {

    /**
     * sets up the testing rule
     */
    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    /**
     * tests to make sure the initial elements are set up, such as the advisor screen existing,
     * text field is there, and send button is present but not enabled yet
     */
    @Test
    fun testVerifyInitialElements() {
        composeTestRule.setContent {
            AdvisorScreen(viewModel = HomeViewModel())
        }

        composeTestRule.onNodeWithTag("advisor_screen").assertExists()
        composeTestRule.onNodeWithText("Ask for advice").assertExists()
        composeTestRule.onNodeWithContentDescription("Send").assertExists().assertHasClickAction().assertIsNotEnabled()
    }

    @Test
    fun testSendButtonEnables() {
        composeTestRule.setContent {
            AdvisorScreen(viewModel = HomeViewModel())
        }

        composeTestRule.onNodeWithContentDescription("Send").assertIsNotEnabled()
        composeTestRule.onNodeWithText("Ask for advice").performTextInput("testing")
        composeTestRule.onNodeWithContentDescription("Send").assertIsEnabled()
    }
}