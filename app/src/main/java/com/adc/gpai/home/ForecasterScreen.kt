package com.adc.gpai.home

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.adc.gpai.R
import com.adc.gpai.models.Course
import com.adc.gpai.models.Term
import com.adc.gpai.models.Transcript
import com.adc.gpai.onboarding.TranscriptRepository
import com.adc.gpai.ui.theme.BrandDarkPurple
import com.adc.gpai.ui.theme.BrandPurple
import com.adc.gpai.ui.theme.GPAiTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun ForecasterScreen() {
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

    // State for edit operations and dialogs
    var editingCourse by remember { mutableStateOf<CourseEditState?>(null) }
    var courseToDelete by remember { mutableStateOf<CourseDeleteState?>(null) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var addingToTermIndex by remember { mutableIntStateOf(0) }

    // Available grade options
    val gradeOptions = listOf("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F")

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            // Cumulative GPA Display at the top
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 16.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(BrandPurple)
                        .padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "GPA Forecaster",
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    val locale = LocalContext.current.resources.configuration.locales.get(0)
                    Text(
                        text = "Cumulative GPA: ${String.format(locale, "%.2f", tempTranscript.gpa)}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            // Scrollable list of terms
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(tempTranscript.terms) { termIndex, term ->
                    EnhancedTermSection(
                        term = term,
                        termIndex = termIndex,
                        isCurrentSemester = termIndex == tempTranscript.terms.size - 1, // Last semester is current
                        onUpdateTerm = { newTerm ->
                            tempTranscript = tempTranscript.copy(
                                terms = tempTranscript.terms.let {
                                    val terms = it.toMutableList()
                                    terms[termIndex] = newTerm
                                    terms
                                }
                            )
                        },
                        onEditCourse = { courseIndex, course ->
                            editingCourse = CourseEditState(
                                termIndex = termIndex,
                                courseIndex = courseIndex,
                                course = course
                            )
                        },
                        onDeleteCourse = { course ->
                            courseToDelete = CourseDeleteState(course = course)
                        },
                        onAddCourse = {
                            addingToTermIndex = termIndex
                            showAddCourseDialog = true
                        }
                    )
                }
            }
        }

        // Edit Course Dialog
        if (editingCourse != null) {
            CourseDialog(
                title = "Edit Course",
                initialCourseName = editingCourse!!.course.courseName,
                initialCourseCode = editingCourse!!.course.courseCode,
                initialCreditHours = editingCourse!!.course.attempted.toString(),
                initialGrade = editingCourse!!.course.grade,
                gradeOptions = gradeOptions,
                onDismiss = { editingCourse = null },
                onConfirm = { courseCode, name, hours, grade ->
                    val course = editingCourse!!.course
                    val updatedCourse = course.copy(
                        courseCode = courseCode,
                        courseName = name,
                        attempted = hours.toIntOrNull() ?: 0,
                        earned = hours.toIntOrNull() ?: 0,
                        grade = grade,
                        points = calculatePoints(hours.toIntOrNull() ?: 0, grade)
                    )

                    // Update in temporary transcript
                    val termIndex = editingCourse!!.termIndex
                    val courseIndex = editingCourse!!.courseIndex
                    tempTranscript = tempTranscript.copy(
                        terms = tempTranscript.terms.let { terms ->
                            val updatedTerms = terms.toMutableList()
                            val term = updatedTerms[termIndex]
                            val updatedCourses = term.courses.toMutableList()
                            updatedCourses[courseIndex] = updatedCourse
                            updatedTerms[termIndex] = term.copy(courses = updatedCourses)
                            updatedTerms
                        }
                    )

                    editingCourse = null
                }
            )
        }

        // Add Course Dialog
        if (showAddCourseDialog) {
            CourseDialogWithTermSelection(
                title = "Add Course",
                initialCourseName = "",
                initialCourseCode = "",
                initialCreditHours = "3",
                initialGrade = "A",
                gradeOptions = gradeOptions,
                terms = tempTranscript.terms,
                initialTermIndex = addingToTermIndex,
                onDismiss = { showAddCourseDialog = false },
                onConfirm = { termId, courseCode, name, hours, grade ->
                    if (name.isNotBlank() && hours.isNotBlank()) {
                        val points = calculatePoints(hours.toIntOrNull() ?: 0, grade)
                        val newCourse = Course(
                            courseCode = courseCode,
                            courseName = name,
                            attempted = hours.toIntOrNull() ?: 0,
                            earned = hours.toIntOrNull() ?: 0,
                            points = points,
                            grade = grade
                        )

                        // Add to temporary transcript
                        tempTranscript = tempTranscript.copy(
                            terms = tempTranscript.terms.map { term ->
                                if (term.id == termId) {
                                    term.copy(courses = term.courses + newCourse)
                                } else {
                                    term
                                }
                            }
                        )

                        showAddCourseDialog = false
                    }
                }
            )
        }

        // Delete Confirmation Dialog
        if (courseToDelete != null) {
            AlertDialog(
                onDismissRequest = { courseToDelete = null },
                title = { Text("Confirm Deletion") },
                text = { Text("Are you sure you want to delete this course: ${courseToDelete!!.course.courseName}?") },
                confirmButton = {
                    Button(
                        onClick = {
                            val courseToRemove = courseToDelete!!.course
                            tempTranscript = tempTranscript.copy(
                                terms = tempTranscript.terms.map { term ->
                                    term.copy(courses = term.courses.filter { it != courseToRemove })
                                }
                            )
                            courseToDelete = null
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple)
                    ) {
                        Text("Delete")
                    }
                },
                dismissButton = {
                    TextButton(onClick = { courseToDelete = null }) {
                        Text("Cancel")
                    }
                }
            )
        }
    }
}

@Composable
fun EnhancedTermSection(
    term: Term,
    termIndex: Int,
    isCurrentSemester: Boolean,
    onUpdateTerm: (Term) -> Unit,
    onEditCourse: (Int, Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onAddCourse: () -> Unit
) {
    var isExpanded by remember { mutableStateOf(isCurrentSemester) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
            .padding(16.dp)
            .animateContentSize(
                animationSpec = tween(
                    durationMillis = 300,
                    easing = LinearOutSlowInEasing
                )
            )
    ) {
            // Term header - always visible
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { isExpanded = !isExpanded },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = term.name + if (isCurrentSemester) " (Current)" else "",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isCurrentSemester) BrandPurple else Color.Black
                    )
                    val locale = LocalContext.current.resources.configuration.locales.get(0)
                    Text(
                        text = "Semester GPA: ${String.format(locale, "%.2f", term.gpa)}",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                        color = Color.Gray
                    )
                }

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = BrandPurple
                    )
                }
            }

            // Expandable content
            if (isExpanded) {
                Spacer(modifier = Modifier.height(12.dp))

                // Courses in this term
                term.courses.forEachIndexed { courseIndex, course ->
                    EnhancedCourseItem(
                        course = course,
                        onGradeChange = { newGrade ->
                            val updatedCourse = course.copy(
                                grade = newGrade,
                                points = calculatePoints(course.attempted, newGrade)
                            )
                            val updatedCourses = term.courses.toMutableList()
                            updatedCourses[courseIndex] = updatedCourse
                            onUpdateTerm(term.copy(courses = updatedCourses))
                        },
                        onEdit = { onEditCourse(courseIndex, course) },
                        onDelete = { onDeleteCourse(course) }
                    )
                    if (courseIndex < term.courses.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }

                // Add course button
                Spacer(modifier = Modifier.height(12.dp))
                Button(
                    onClick = onAddCourse,
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "Add Course"
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(text = "Add Course")
                }
            }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedCourseItem(
    course: Course,
    onGradeChange: (String) -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(8.dp))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Course info
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${course.courseCode} ${course.courseName}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )
            Text(
                text = "${course.attempted} credits",
                fontSize = 12.sp,
                color = Color.Gray
            )
        }

        // Grade dropdown selector
        var gradeExpanded by remember { mutableStateOf(false) }
        val gradeOptions = listOf("A+", "A", "A-", "B+", "B", "B-", "C+", "C", "C-", "D+", "D", "D-", "F")
        
        ExposedDropdownMenuBox(
            expanded = gradeExpanded,
            onExpandedChange = { gradeExpanded = !gradeExpanded },
            modifier = Modifier
                .padding(end = 8.dp)
                .width(50.dp)
        ) {
            Surface(
                shape = RoundedCornerShape(6.dp),
                color = when (course.grade.firstOrNull() ?: '?') {
                    'A', 'S' -> Color(0xFF4CAF50) // Green
                    'B' -> Color(0xFF8BC34A) // Light Green
                    'C' -> Color(0xFFFFC107) // Yellow
                    'D' -> Color(0xFFFF9800) // Orange
                    else -> Color(0xFFF44336) // Red for F
                },
                modifier = Modifier.menuAnchor()
            ) {
                Text(
                    text = course.grade,
                    textAlign = TextAlign.Center,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                )
            }
            
            ExposedDropdownMenu(
                expanded = gradeExpanded,
                onDismissRequest = { gradeExpanded = false },
                modifier = Modifier.width(50.dp)
            ) {
                gradeOptions.forEach { grade ->
                    DropdownMenuItem(
                        text = { 
                            Text(
                                text = grade,
                                modifier = Modifier.fillMaxWidth()
                            ) 
                        },
                        onClick = {
                            onGradeChange(grade)
                            gradeExpanded = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // Action buttons
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit Course",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete Course",
                tint = MaterialTheme.colorScheme.error,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

// Reusing dialog components from ModifyTranscriptScreen
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialog(
    title: String,
    initialCourseName: String,
    initialCourseCode: String,
    initialCreditHours: String,
    initialGrade: String,
    gradeOptions: List<String>,
    onDismiss: () -> Unit,
    onConfirm: (courseCode: String, courseName: String, creditHours: String, grade: String) -> Unit
) {
    var courseName by remember { mutableStateOf(initialCourseName) }
    var courseCode by remember { mutableStateOf(initialCourseCode) }
    var creditHours by remember { mutableStateOf(initialCreditHours) }
    var selectedGrade by remember { mutableStateOf(initialGrade) }
    var expanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = creditHours,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            creditHours = it
                        }
                    },
                    label = { Text("Credit Hours") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = expanded,
                    onExpandedChange = { expanded = !expanded }
                ) {
                    TextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = expanded,
                        onDismissRequest = { expanded = false }
                    ) {
                        gradeOptions.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    selectedGrade = grade
                                    expanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (courseName.isNotBlank() && creditHours.isNotBlank()) {
                                onConfirm(courseCode, courseName, creditHours, selectedGrade)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                        enabled = courseName.isNotBlank() && creditHours.isNotBlank()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialogWithTermSelection(
    title: String,
    initialCourseName: String,
    initialCourseCode: String,
    initialCreditHours: String,
    initialGrade: String,
    gradeOptions: List<String>,
    terms: List<Term>,
    initialTermIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (termId: Int, courseCode: String, courseName: String, creditHours: String, grade: String) -> Unit
) {
    var courseName by remember { mutableStateOf(initialCourseName) }
    var courseCode by remember { mutableStateOf(initialCourseCode) }
    var creditHours by remember { mutableStateOf(initialCreditHours) }
    var selectedGrade by remember { mutableStateOf(initialGrade) }
    var selectedTermIndex by remember { mutableStateOf(initialTermIndex) }

    var gradeExpanded by remember { mutableStateOf(false) }
    var termExpanded by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = title,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = termExpanded,
                    onExpandedChange = { termExpanded = !termExpanded }
                ) {
                    TextField(
                        value = if (terms.isNotEmpty() && selectedTermIndex < terms.size)
                            terms[selectedTermIndex].name
                        else "Select Term",
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Term") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = termExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = termExpanded,
                        onDismissRequest = { termExpanded = false }
                    ) {
                        terms.forEachIndexed { index, term ->
                            DropdownMenuItem(
                                text = { Text(term.name) },
                                onClick = {
                                    selectedTermIndex = index
                                    termExpanded = false
                                }
                            )
                        }
                    }
                }

                TextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = creditHours,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            creditHours = it
                        }
                    },
                    label = { Text("Credit Hours") },
                    modifier = Modifier.fillMaxWidth()
                )

                ExposedDropdownMenuBox(
                    expanded = gradeExpanded,
                    onExpandedChange = { gradeExpanded = !gradeExpanded }
                ) {
                    TextField(
                        value = selectedGrade,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Grade") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = gradeExpanded) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .menuAnchor()
                    )

                    ExposedDropdownMenu(
                        expanded = gradeExpanded,
                        onDismissRequest = { gradeExpanded = false }
                    ) {
                        gradeOptions.forEach { grade ->
                            DropdownMenuItem(
                                text = { Text(grade) },
                                onClick = {
                                    selectedGrade = grade
                                    gradeExpanded = false
                                }
                            )
                        }
                    }
                }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancel")
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Button(
                        onClick = {
                            if (courseName.isNotBlank() && creditHours.isNotBlank() && terms.isNotEmpty()) {
                                val termId = terms[selectedTermIndex].id
                                onConfirm(termId, courseCode, courseName, creditHours, selectedGrade)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                        enabled = courseName.isNotBlank() && creditHours.isNotBlank() && terms.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

// Helper function to calculate grade points
private fun calculatePoints(creditHours: Int, grade: String): Double {
    val pointsPerCredit = when (grade) {
        "A+" -> 4.33
        "A" -> 4.0
        "A-" -> 3.67
        "B+" -> 3.33
        "B" -> 3.0
        "B-" -> 2.67
        "C+" -> 2.33
        "C" -> 2.0
        "C-" -> 1.67
        "D+" -> 1.33
        "D" -> 1.0
        "D-" -> 0.67
        "F" -> 0.0
        else -> 0.0
    }

    return creditHours * pointsPerCredit
}

// Data classes for UI state
data class CourseEditState(
    val termIndex: Int,
    val courseIndex: Int,
    val course: Course
)

data class CourseDeleteState(
    val course: Course
)

// Convert numeric grade to letter equivalent (kept for compatibility)
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