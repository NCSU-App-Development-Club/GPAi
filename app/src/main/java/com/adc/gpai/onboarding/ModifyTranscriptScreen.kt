package com.adc.gpai.onboarding

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.adc.gpai.home.HomeActivity
import com.adc.gpai.models.Transcript
import com.adc.gpai.ui.theme.GPAiTheme

@Composable
fun ModifyTranscriptScreen(navController: NavHostController? = null, viewModel: OnboardingViewModel) {
    val context = LocalContext.current
    val transcript = viewModel.transcript.observeAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("modify_screen")
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Course List", style = MaterialTheme.typography.headlineLarge)
        CourseList(transcript = transcript.value)
        Button(onClick = { context.startActivity(Intent(context, HomeActivity::class.java)) }) {
            Text(text = "Finish")
        }
    }
}

/**
 * Composable function to display a list of courses in a transcript.
 * If no transcript is uploaded, it displays a placeholder message.
 *
 * @param transcript The parsed transcript data to display.
 * @param modifier Modifier to apply custom styling.
 */
@Composable
fun CourseList(transcript: Transcript?, modifier: Modifier = Modifier) {
    if (transcript != null) {
        // Scrollable column to display the transcript data.
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            for (term in transcript.terms) {
                // Display the term name (e.g., "Fall 2023").
                Text(
                    text = term.name,
                    style = MaterialTheme.typography.headlineMedium
                )
                for (course in term.courses) {
                    // Display course information in a row: course code, name, credits, and grade.
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = course.courseCode, fontWeight = FontWeight.Bold) // Course code.
                        Text(
                            text = course.courseName,
                            textAlign = TextAlign.Start,
                            modifier = Modifier
                                .weight(1f)
                                .padding(horizontal = 8.dp)
                        )
                        Text(
                            text = course.attempted.toString(),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(horizontal = 8.dp)
                        )
                        Text(
                            text = String.format("%-2s", course.grade),
                            textAlign = TextAlign.End,
                            modifier = Modifier.padding(end = 8.dp)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp)) // Add space between terms.
            }
        }
    } else {
        // If no transcript is available, show a placeholder message.
        Text(text = "No transcript uploaded", modifier = modifier)
    }
}

@Preview(showBackground = true)
@Composable
fun ModifyTranscriptPreview() {
    GPAiTheme {
        ModifyTranscriptScreen(navController = null, viewModel = OnboardingViewModel(
            OnboardingViewModel.sampleTranscript))
    }
}