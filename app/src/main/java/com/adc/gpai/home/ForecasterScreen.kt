package com.adc.gpai.home

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adc.gpai.R
import com.adc.gpai.models.Transcript
import com.adc.gpai.onboarding.TranscriptRepository
import com.adc.gpai.ui.theme.GPAiTheme
import org.koin.androidx.compose.koinViewModel


// TODO - on clicking on calculate button, calculate semester gpa, and update transcript repository to then calculate cumulative GPA
// TODO - make calculate button obviously unclickable if user has not changed anything about the screen
// TODO - enhance layout (at last)

@Composable
fun ForecasterScreen() {
    // transcript repository that persists changes throughout app
    // it contains a list of term objects, which each contain their respective courses
    val viewModel: TranscriptRepository = koinViewModel()

    val transcript = viewModel.transcript.observeAsState()
    var mostRecentTerm = transcript.value?.terms?.last()
    var courses = mostRecentTerm?.courses ?: emptyList()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = mostRecentTerm?.name ?: "Current Semester", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Iterate over courses and display each with a delete option
        courses.forEach { course ->
            CourseEntry(
                courseCode = course.courseCode,
                courseName = course.courseName,
                onDelete = { viewModel.removeCourse(course) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // TODO : make this dynamic - call gpa field from Transcript object..
        var cumGPA = transcript.value?.gpa

        Text(text = "Cumulative GPA: $cumGPA", style = MaterialTheme.typography.bodySmall)
        Text(text = "Semester GPA: 4.0", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            // TODO : Add button for new course
            Button(onClick = {
                // Pseudocode
                // Update TranscriptRepository with adjusted course values,
                // and new courses
                // use TranscriptRepository function to calculate sem gpa and cum gpa

            }) {
                Text(text = "Calculate")
            }

            Button(onClick = {
                //add new course to a list of courses

            }) {
                Text(text = "Add Course")
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                // Pseudocode
                // Check if on Forecaster, if not, navigate to Forecaster
            }) {
                Text(text = "Forecaster")
            }

            Button(onClick = {
                // Pseudocode
                // Check if on Advisor, if not, navigate to Advisor
            }) {
                Text(text = "Advisor")
            }
        }
    }
}

/***
 * Event handler for calculate button, it does the following:
 * Updates TranscriptRepository for latest term
 * Updates cumulative and semester GPAs
 */
@Composable
fun CourseEntry(courseCode: String, courseName: String, onDelete: () -> Unit) {
    var grade by remember { mutableStateOf(4.33f) }  // Default grade (A+ = 4.33)
    var units by remember { mutableStateOf(3f) }  // Default units (3)
//    TODO: Modify UI for each entry here
    Column(
        modifier = Modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Course Code: $courseCode")
                Text(text = "Course Name: $courseName")
            }
            IconButton(onClick = { onDelete() }) {
                Icon(
                    painter = painterResource(id = R.drawable.error),
                    contentDescription = "Delete Course",
                    tint = Color.Red
                )
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Grade: ${gradeToLetter(grade)}")
                Slider(
                    value = grade,
                    onValueChange = { grade = it },
                    valueRange = 0f..4.33f,
                    steps = 13,  // A+, A, B+, etc.
                    modifier = Modifier.width(150.dp)
                )
            }
            Column {
                Text(text = "Units: ${units.toInt()}")
                Slider(
                    value = units,
                    onValueChange = { units = it },
                    valueRange = 1f..3f,
                    steps = 2,  // 1 to 5 units
                    modifier = Modifier.width(150.dp)
                )
            }
        }
    }
}

// Convert numeric grade to letter equivalent
fun gradeToLetter(grade: Float): String {
    return when {
        grade >= 4.33f -> "A+"
        grade >= 4.0f -> "A"
        grade >= 3.667f -> "A-"
        grade >= 3.33f -> "B+"
        grade >= 3.0f -> "B"
        grade >= 2.667f -> "B-"
        grade >= 2.333f -> "C+"
        grade >= 2.0f -> "C"
        grade >= 1.667f -> "C-"
        grade >= 1.333f -> "D+"
        grade >= 1.0f -> "D"
        grade >= 0.667f -> "D-"
        else -> "F"
    }
}


@Composable
fun ForecasterScreen(viewModel: TranscriptRepository = koinViewModel()){
    ForecasterScreen(
        viewModel = viewModel
    )
}
@Preview(showBackground = true)
@Composable
fun ForecasterPreview() {
    GPAiTheme {
        ForecasterScreen()
    }
}
