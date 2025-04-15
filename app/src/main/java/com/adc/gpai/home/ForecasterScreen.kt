package com.adc.gpai.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adc.gpai.R
import com.adc.gpai.models.Course
import com.adc.gpai.models.Term
import com.adc.gpai.models.Transcript
import com.adc.gpai.onboarding.TranscriptRepository
import com.adc.gpai.ui.theme.GPAiTheme
import org.koin.androidx.compose.koinViewModel

// TODO - update Transcript Repository whenever update made - sliders adjusted, new course added
// TODO - enhance layout (at last)
// TODO - don't let user click on Add New Course button, if fields have not been entered

@Composable
fun ForecasterScreen() {
    // TODO : exception handling for empty transcript? It's possible for someone to is yet to enroll, but would like to still asses their future grades to have any courses on their transcript
    val viewModel: TranscriptRepository = koinViewModel()

    val transcript = viewModel.transcript.observeAsState()

    // In the forecaster, the user can modify their transcript without saving it.
    // Their edits are kept here. In the future, if we want to add the ability to
    // sync temporary edits to the saved transcript, we can use TranscriptRepository#updateTranscript.
    var tempTranscript by remember {
        mutableStateOf<Transcript>(
            transcript.value ?: Transcript(
                emptyList()
            )
        )
    }

    LaunchedEffect(transcript.value) {
        if (transcript.value != null) {
            tempTranscript = transcript.value!!
        }
    }

    val scrollState = rememberScrollState()
    Column(modifier = Modifier.verticalScroll(scrollState)) {
        for ((i, term) in tempTranscript.terms.withIndex()) {
            Term(term = term, onUpdate = { newTerm ->
                tempTranscript = tempTranscript.copy(
                    terms = tempTranscript.terms.let {
                        val terms = it.toMutableList()
                        terms[i] = newTerm
                        terms
                    })
            })
        }

        Column(modifier = Modifier.padding(16.dp)) {
            val locale = LocalContext.current.resources.configuration.locales.get(0)
            Text(
                text = "Cumulative GPA: ${
                    String.format(
                        locale,
                        "%.2f",
                        tempTranscript.gpa
                    )
                }",
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}

@Composable
fun Term(term: Term, onUpdate: (Term) -> Unit) {
    val viewModel: TranscriptRepository = koinViewModel()
    var openPopup by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = term.name,
            style = MaterialTheme.typography.titleLarge
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Iterate over courses from transcript repository and display each with a delete option
        term.courses.forEachIndexed { i, course ->
            CourseEntry(
                course = course,
                onDelete = {
                    onUpdate(term.copy(courses = term.courses.filterIndexed { j, _ -> j != i }))
                },
                onUpdate = { newCourse ->
                    onUpdate(term.copy(courses = term.courses.let {
                        val courses = term.courses.toMutableList()
                        courses[i] = newCourse
                        courses
                    }))
                })

            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        val locale = LocalContext.current.resources.configuration.locales.get(0)
        Text(
            text = "Semester GPA: ${String.format(locale, "%.2f", term.gpa)}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                openPopup = true
            }) {
                Text(text = "Add Course")
            }
        }

        if (openPopup) {
            DisplayCourseEntryFields(
                onDismiss = { openPopup = false },
                onConfirmation = { course: Course ->
                    viewModel.addCourse(term.id, course)
                })
        }

        HorizontalDivider()
    }
}

/*
 Function to display dialog box/popup for new course entry
 */
@Composable
fun DisplayCourseEntryFields(
    onDismiss: () -> Unit,
    onConfirmation: (Course) -> Unit,
) {
    var courseCode by remember { mutableStateOf("") }
    var courseName by remember { mutableStateOf("") }
    var grade by remember { mutableFloatStateOf(0.0f) }
    var units by remember { mutableFloatStateOf(1.0f) }

    Dialog(onDismissRequest = { onDismiss() }) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    TextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Enter Course Code") })
                }
                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    TextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Enter Course Name") })
                }

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
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
                            valueRange = 1f..5f,
                            steps = 3, // 1 to 5 units
                            modifier = Modifier.width(150.dp)
                        )
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center
                ) {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Button(
                            // passing current values back to ForecastScreen() for processing
                            onClick = {
                                onConfirmation(
                                    Course(
                                        courseCode, courseName, units.toInt(), grade.toInt(),
                                        (units * grade).toDouble(), gradeToLetter(grade)
                                    )
                                )
                            },
                            modifier = Modifier.padding(8.dp),
                        ) {
                            Text("Add Course")
                        }
                    }
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        ElevatedButton(
                            onClick = { onDismiss() },
                            modifier = Modifier.padding(8.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
                        ) {
                            Text("Dismiss Entry")
                        }
                    }

                }
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
fun CourseEntry(course: Course, onUpdate: (Course) -> Unit, onDelete: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.Start
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Course Code: ${course.courseCode}")
                Text(text = "Course Name: ${course.courseName}")
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
            modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(text = "Grade: ${course.grade}")
                Slider(
                    enabled = course.isForGrade(),
                    value = course.points.toFloat() / course.earned.toFloat(),
                    onValueChange = {
                        onUpdate(course.copy(
                            grade = gradeToLetter(it),
                            points = it.toDouble() * course.earned
                        ))
                    },
                    valueRange = 0f..4.33f,
                    steps = 13,  // A+, A, B+, etc.
                    modifier = Modifier.width(150.dp)
                )
            }
            Column {
                Text(text = "Units: ${course.earned.toInt()}")
                Slider(
                    value = course.earned.toFloat(),
                    onValueChange = {
                        onUpdate(course.copy(attempted = it.toInt(), earned = it.toInt()))
                    },
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

fun letterToGrade(letterGrade: String): Float {
    return when (letterGrade) {
        "A+" -> 4.33f
        "A" -> 4.0f
        "A-" -> 3.667f
        "B+" -> 3.33f
        "B" -> 3.0f
        "B-" -> 2.667f
        "C+" -> 2.333f
        "C" -> 2.0f
        "C-" -> 1.667f
        "D+" -> 1.333f
        "D" -> 1.0f
        "D-" -> 0.667f
        else -> 0.00F
    }
}

@Preview(showBackground = true)
@Composable
fun ForecasterPreview() {
    GPAiTheme {
        ForecasterScreen()
    }
}
