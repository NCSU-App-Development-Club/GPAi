package com.adc.gpai

/**
 * This class contains UI tests for the Forecaster View in the app.
 * It tests the functionality of creating new courses, setting their grades and course units
 * and then calculating the Cumulative and Semester gpa
 *
 * @author Kerneep Sandhu
 */
@RunWith(AndroidJUnit4::class)
class ForecasterViewUITest {

//    TODO: need to confirm whether rule needs to be configured to specific activity
    @get:Rule
    val composeTestRule = createComposeRule()

    //scenarios to test
//    course added successfully
//    course details that were added match what is displayed
//    slider has options for all grades and their relative positions are as expected
//    (for example - A+ has to be completely on the right)
//    cumulative and semester gpas are calculated correctly
//        - after adding course
//        - after removing course
//        - having only 1 unit courses
//        - having only 3 unit courses
//        - having multi-unit courses
//    course removed successfully

    /**
     * Test method for confirming existence of necessary UI elements when first
     * opening the forecaster screen
     */
    @Test
    fun ui_validation(){
        composeTestRule.setContent {
            MyComposable()
        }
//        verify ui elements display:
//        settings button
        val settingsButton = composeTestRule.onNodeWithContentDescription("Settings")
        settingsButton.assert(hasSemantics {
            this.role == Role.Button
        })
//  TODO: if using tag instead, uncomment line underneath:
    //       composeTestRule.onNodeWithTag("Settings").assertIsDisplayed()
        settingsButton.assertIsDisplayed()
        settingsButton.assertIsEnabled()
        settingsButton.assert(hasClickAction())

//        "GPAi" title
        composeTestRule.onNodeWithText("GPAi")
            .assertIsDisplayed() // Check if the title is visible
//        "Current Semester" title
        composeTestRule.onNodeWithText("Current Semester")
            .assertIsDisplayed()
//        TODO: confirm title stylings

//        "Cumulative GPA" label with some val
        composeTestRule.onNodeWithText("Cumulative GPA")
            .assertIsDisplayed()
//        TODO: what value to display?
//        "Semester GPA" label with some val
        composeTestRule.onNodeWithText("Semester GPA")
            .assertIsDisplayed()
        //        TODO: what value to display?

//        Calculate button
        val calculateButton = composeTestRule.onNodeWithText("Calculate")
        calculateButton.assert(hasSemantics {
            this.role == Role.Button
        })
        calculateButton.assertIsDisplayed()
        calculateButton.assertIsDisabled()
        calculateButton.assert(hasClickAction())
//        Plus (Add Course) button
        val addCourseButton = composeTestRule.onNodeWithContentDescription("Add Course")
        addCourseButton.assert(hasSemantics {
            this.role == Role.Button
        })
        addCourseButton.assertIsDisplayed()
        addCourseButton.assertIsEnabled()
        addCourseButton.assert(hasClickAction())

//        Forecaster/Advisor button thingy
//        TODO: confirm what the Forecaster/Advisor thing will be coded as

        val forecastButton = composeTestRule.onNodeWithContentDescription("Forecast")
        forecastButton.assert(hasSemantics {
            this.role == Role.Button
        })
        forecastButton.assertIsDisplayed()
        forecastButton.assertIsDisabled()
        forecastButton.assert(hasClickAction())
    }


    /**
     * Test method for adding a course with the plus button
     *
     * Scenarios covered:
     * 1 - Adding a course for the first time
     */
    @Test
    fun add_course_with_+_button(){
        composeTestRule.setContent {
            MyComposable()
        }
        val addCourseButton = composeTestRule.onNodeWithContentDescription("Add Course")
        addCourseButton.performClick()
// TODO: need to confirm what view looks like after clicking on addCourseButton
        composeTestRule.onNodeWithText("Course Code").performTextInput("CSC 116")
        composeTestRule.onNodeWithText("Course Name").performTextInput("Introduction to Java")
        composeTestRule.onNodeWithText("Grade").performScrollTo().performValueChange(100f)
        composeTestRule.onNodeWithText("Units").performScrollTo().performValueChange(100f)
// TODO: need to confirm what values the sliders will be configured to - discrete or continuous, and what range
        composeTestRule.onNodeWithContentDescription("Add Course").performClick()
        val cancelButton = composeTestRule.onNodeWithContentDescription("Cancel")
        cancelButton.assert(hasSemantics {
            this.role == Role.Button
        }).
        cancelButton.assertIsDisplayed()
        cancelButton.assertIsEnabled()
        cancelButton.assert(hasClickAction())

//        At this point, a course has been added
//        Now, confirming that home view looks as intended with all existing UI elements still displayed

        val settingsButton = composeTestRule.onNodeWithContentDescription("Settings")
        settingsButton.assert(hasSemantics {
            this.role == Role.Button
        })

//        confirm remove button presence
        val removeButtonCSC116 = composeTestRule.onNodeWithContentDescription("removeCSC116")
        removeButtonCSC116.assert(hasSemantics {
            this.role == Role.Button
        })
        removeButtonCSC116.assertIsDisplayed()
        removeButtonCSC116.assertIsEnabled()
        removeButtonCSC116.assert(hasClickAction())

//  TODO: if using tag instead, uncomment line underneath:
        //       composeTestRule.onNodeWithTag("Settings").assertIsDisplayed()
        settingsButton.assertIsDisplayed()
        settingsButton.assertIsEnabled()
        settingsButton.assert(hasClickAction())

//        "GPAi" title
        composeTestRule.onNodeWithText("GPAi")
            .assertIsDisplayed() // Check if the title is visible
//        "Current Semester" title
        composeTestRule.onNodeWithText("Current Semester")
            .assertIsDisplayed()
//        TODO: confirm title stylings

//        "Cumulative GPA" label with some val
        composeTestRule.onNodeWithText("Cumulative GPA")
            .assertIsDisplayed()
//        TODO: what value to display?
//        "Semester GPA" label with some val
        composeTestRule.onNodeWithText("Semester GPA")
            .assertIsDisplayed()
        //        TODO: what value to display?

//        Calculate button
        val calculateButton = composeTestRule.onNodeWithText("Calculate")
        calculateButton.assert(hasSemantics {
            this.role == Role.Button
        })
        calculateButton.assertIsDisplayed()
        calculateButton.assertIsDisabled()
        calculateButton.assert(hasClickAction())
//        Plus (Add Course) button
        val addCourseButton = composeTestRule.onNodeWithContentDescription("Add Course")
        addCourseButton.assert(hasSemantics {
            this.role == Role.Button
        })
        addCourseButton.assertIsDisplayed()
        addCourseButton.assertIsEnabled()
        addCourseButton.assert(hasClickAction())


//        Forecaster/Advisor button thingy
//        TODO: confirm what the Forecaster/Advisor thing will be coded as
        val forecastButton = composeTestRule.onNodeWithContentDescription("Forecast")
        forecastButton.assert(hasSemantics {
            this.role == Role.Button
        })
        forecastButton.assertIsDisplayed()
        forecastButton.assertIsDisabled()
        forecastButton.assert(hasClickAction())
    }

    /**
     * Test method for removing a course with its X button
     *
     * Scenarios covered:
     * 1 - removing the 1st out of 2 courses on the screen
     */
    @Test
    fun remove_course_with_X_button(){
        val addCourseButton = composeTestRule.onNodeWithContentDescription("Add Course")
        addCourseButton.performClick()
// TODO: need to confirm what view looks like after clicking on addCourseButton
        composeTestRule.onNodeWithText("Course Code").performTextInput("CSC 116")
        composeTestRule.onNodeWithText("Course Name").performTextInput("Introduction to Java")
        composeTestRule.onNodeWithText("Grade").performScrollTo().performValueChange(100f)
        composeTestRule.onNodeWithText("Units").performScrollTo().performValueChange(100f)
// TODO: need to confirm what values the sliders will be configured to - discrete or continuous, and what range
        composeTestRule.onNodeWithContentDescription("Add Course").performClick()

        val addCourseButton = composeTestRule.onNodeWithContentDescription("Add Course")
        addCourseButton.performClick()
// TODO: need to confirm what view looks like after clicking on addCourseButton
        composeTestRule.onNodeWithText("Course Code").performTextInput("CSC 216")
        composeTestRule.onNodeWithText("Course Name").performTextInput("Software Development Fundamentals")
        composeTestRule.onNodeWithText("Grade").performScrollTo().performValueChange(100f)
        composeTestRule.onNodeWithText("Units").performScrollTo().performValueChange(100f)
// TODO: need to confirm what values the sliders will be configured to - discrete or continuous, and what range
        composeTestRule.onNodeWithContentDescription("Add Course").performClick()

//        Removing CSC 116
        composeTestRule.onNodeWithText("CSC 116").assertIsDisplayed()
        composeTestRule.onNodeWithText("Introduction to Java").assertIsDisplayed()
        composeTestRule.onNodeWithText("CSC 216").assertIsDisplayed()
        composeTestRule.onNodeWithText("Software Development Fundamentals").assertIsDisplayed()
// TODO: confirm whether presence of all UI elements needs to be confirmed

        val removeButtonCSC116 = composeTestRule.onNodeWithContentDescription("removeCSC116")
        removeButtonCSC116.performClick()

        composeTestRule.onNodeWithText("CSC 116").assertDoesNotExist()
        composeTestRule.onNodeWithText("Introduction to Java").assertDoesNotExist()
        composeTestRule.onNodeWithText("CSC 216").assertIsDisplayed()
        composeTestRule.onNodeWithText("Software Development Fundamentals").assertIsDisplayed()

        val removeButtonCSC216 = composeTestRule.onNodeWithContentDescription("removeCSC216")
        removeButtonCSC216.performClick()
        composeTestRule.onNodeWithText("CSC 216").assertDoesNotExist()
        composeTestRule.onNodeWithText("Software Development Fundamentals").assertDoesNotExist()

    }

    @Test
    fun adjust_course_grade_and_units_with_slider(){

    }
    
    @Test
    fun calculate_cum_and_sem_gpa_with_calculate_button(){

    }



}