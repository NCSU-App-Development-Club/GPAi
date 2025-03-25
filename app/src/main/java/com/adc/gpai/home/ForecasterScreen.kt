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
import com.adc.gpai.models.Course
import com.adc.gpai.ui.theme.GPAiTheme

@Composable
fun ForecasterScreen() {
    // Mutable list of courses
    var courses by remember {
        mutableStateOf(
            listOf(
                Course("CSC 408", "Software Product Management"),
                Course("CSC 495", "Animal Centered Computing"),
                Course("CSC 295", "Applications in Python")
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Text(text = "Current Semester", style = MaterialTheme.typography.bodyMedium)

        Spacer(modifier = Modifier.height(16.dp))

        // Iterate over courses and display each with a delete option
        courses.forEach { course ->
            CourseEntry(
                courseCode = course.code,
                courseName = course.name,
                onDelete = { courses = courses.filter { it != course } }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Display cumulative and semester GPA (currently static)
        Text(text = "Cumulative GPA: 3.638", style = MaterialTheme.typography.bodySmall)
        Text(text = "Semester GPA: 4.0", style = MaterialTheme.typography.bodySmall)

        Spacer(modifier = Modifier.height(16.dp))

        Row(
            horizontalArrangement = Arrangement.SpaceEvenly,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(onClick = { /* Calculate action */ }) {
                Text(text = "Calculate")
            }

            Button(onClick = { /* Navigate to Forecaster */ }) {
                Text(text = "Forecaster")
            }

            Button(onClick = { /* Navigate to Advisor */ }) {
                Text(text = "Advisor")
            }
        }
    }
}

@Composable
fun CourseEntry(courseCode: String, courseName: String, onDelete: () -> Unit) {
    var grade by remember { mutableStateOf(4f) }  // Default grade (A+ = 4.0)
    var units by remember { mutableStateOf(3f) }  // Default units (3)

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
                    painter = painterResource(id = R.drawable.check),
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
                    valueRange = 0f..4f,
                    steps = 5,  // A+, A, B+, etc.
                    modifier = Modifier.width(150.dp)
                )
            }
            Column {
                Text(text = "Units: ${units.toInt()}")
                Slider(
                    value = units,
                    onValueChange = { units = it },
                    valueRange = 0f..3f,
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
        grade >= 3.67f -> "A+"
        grade >= 3.33f -> "A"
        grade >= 3.00f -> "A-"
        grade >= 2.67f -> "B+"
        else -> "B"
    }
}

@Preview(showBackground = true)
@Composable
fun ForecasterPreview() {
    GPAiTheme {
        ForecasterScreen()
    }
}
