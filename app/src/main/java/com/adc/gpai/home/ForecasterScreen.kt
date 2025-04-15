package com.adc.gpai.home

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.ElevatedButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
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
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavDeepLinkRequest
import com.adc.gpai.R
import com.adc.gpai.models.Transcript
import com.adc.gpai.onboarding.TranscriptRepository
import com.adc.gpai.ui.theme.GPAiTheme
import org.koin.androidx.compose.koinViewModel
import androidx.navigation.NavHostController
import com.adc.gpai.models.Course


// TODO - on clicking on calculate button, calculate semester gpa, and update transcript repository to then calculate cumulative GPA
// TODO - make calculate button obviously unclickable if user has not changed anything about the screen
// TODO - enhance layout (at last)
// TODO - don't let user click on Add New Course button, if fields have not been entered

@Composable
fun ForecasterScreen(navController: NavHostController? = null) {
    // transcript repository that persists changes throughout app
    // it contains a list of term objects, which each contain their respective courses

    // TODO : exception handling?
    val viewModel: TranscriptRepository = koinViewModel()

    val transcript = viewModel.transcript.observeAsState()
    var mostRecentTerm = transcript.value?.terms?.last()
    var courses = mostRecentTerm?.courses ?: emptyList()
    var openPopup by remember { mutableStateOf(false) }
    var newCourseAdded by remember { mutableStateOf(false) }
    var newCourse by remember { mutableStateOf(
        Course(
            courseCode = "default",
            courseName = "default",
            points = 0.0,
            grade = "Z")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = mostRecentTerm?.name ?: "Current Semester", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Iterate over courses from transcript repository and display each with a delete option
        courses.forEach { course ->
            CourseEntry(
                courseCode = course.courseCode,
                courseName = course.courseName,
                onDelete = { viewModel.removeCourse(course) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        // TODO : Iterate over courses added by user within the screen itself (this course list is erased when clicked on Calculate button)

        Spacer(modifier = Modifier.height(32.dp))

        // TODO : make this dynamic - call gpa field from Transcript object..
        var cumGPA = transcript.value?.gpa
        // TODO : handle semester gpa
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
                // create user input text fields for course code, name
                //add new course to the list of courses
                openPopup = true
            }) {
                Text(text = "Add Course")
            }
        }

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = {
                // Add check for if already there?
                navController?.navigate("forecaster")
            }) {
                Text(text = "Forecaster")
            }

            Button(onClick = {
                // Add check for if already there?
                navController?.navigate("advisor")
            }) {
                Text(text = "Advisor")
            }
        }

        if (openPopup){
            DisplayCourseEntryFields(
                onDismiss = {openPopup = false},
                onConfirmation = {
                    newCourseCode, newCourseName, ->
                    newCourse = Course(
                        courseCode = newCourseCode,
                        courseName = newCourseName,
                        points = 4.33,
                        grade = "A+"
                    )
                    newCourseAdded = true
                 }
            )
        }

        if (newCourseAdded){
            // TODO: on delete function only needs to get rid of this specific course entry from
            // display page, not the transcript
            // HOWEVER, what happens after transcript is updated in Calculate button? Do the courses that were not originally
            // part of the transcript disappear, or are there duplicate entries?

            CourseEntry(
                courseCode = newCourse.courseCode,
                courseName = newCourse.courseName,
                onDelete = { viewModel.removeCourse(newCourse)}
            )

            newCourseAdded = false
        }
    }
}

/*
 Function to display dialog box/popup for new course entry
 */
@Composable
fun DisplayCourseEntryFields(
    // pass in function to close dialog box without any updates...
    onDismiss : () -> Unit,
    onConfirmation: (String, String) -> Unit, // pass in function to update list of courses in transcript?
){
     Dialog( onDismissRequest = { onDismiss() })
     {
        var courseCode = ""
        var courseName = ""
        var grade = 0.0f
        var units = 1.0f

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .height(375.dp)
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
        ){
            Column(
                modifier = Modifier
                    .fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ){
                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){

                    TextField(
                        value = courseCode,
                        onValueChange = { courseCode = it },
                        label = { Text("Enter Course Code")}
                    )
                }
                Row (
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    TextField(
                        value = courseName,
                        onValueChange = { courseName = it },
                        label = { Text("Enter Course Name")}
                    )
                }


//                Row(
//                    modifier = Modifier
//                        .fillMaxWidth(),
//                    horizontalArrangement = Arrangement.Center
//                ){
//                    Column {
//                        Text(text = "Grade: ${gradeToLetter(grade)}")
//                        Slider(
//                            value = grade,
//                            onValueChange = { grade = it },
//                            valueRange = 0f..4.33f,
//                            steps = 13,  // A+, A, B+, etc.
//                            modifier = Modifier.width(150.dp)
//                        )
//                    }
//                    Column {
//                        Text(text = "Units: ${units.toInt()}")
//                        Slider(
//                            value = units,
//                            onValueChange = { units = it },
//                            valueRange = 1f..3f,
//                            steps = 2,  // 1 to 5 units
//                            modifier = Modifier.width(150.dp)
//                        )
//                    }
//                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center
                ){
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ){
                        ElevatedButton(
                            // passing current values back to ForecastScreen() for processing
                            onClick = {onConfirmation(courseCode, courseName)},
                            modifier = Modifier.padding((8.dp)),
                            colors = ButtonDefaults.buttonColors(containerColor = Color.Green)
                        ) {
                            Text("Add Course!")
                        }
                    }
                    Column(
                        modifier = Modifier
                            .fillMaxSize(),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ){
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
fun CourseEntry(courseCode: String, courseName: String, onDelete: () -> Unit) {
    var grade by remember { mutableStateOf(4.33f) }  // Default grade (A+ = 4.33)
    var units by remember { mutableStateOf(3f) }  // Default units (3)
//    TODO: Modify UI to better match Figma design for each entry here
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


fun letterToGrade(letterGrade: String) : Float {
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

// TODO : need to handle viewModel interaction with Preview annotation
//@Composable
//fun ForecasterScreen(viewModel: TranscriptRepository = koinViewModel()){
//    ForecasterScreen(
//        viewModel = viewModel
//    )
//}


@Preview(showBackground = true)
@Composable
fun ForecasterPreview() {
    GPAiTheme {
        ForecasterScreen()
    }
}
