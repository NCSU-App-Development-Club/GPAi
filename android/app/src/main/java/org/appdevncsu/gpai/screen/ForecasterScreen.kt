package org.appdevncsu.gpai.screen

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
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import org.appdevncsu.gpai.models.Course
import org.appdevncsu.gpai.models.Term
import org.appdevncsu.gpai.models.Transcript
import org.appdevncsu.gpai.ui.theme.BrandDarkPurple
import org.appdevncsu.gpai.ui.theme.BrandPurple
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.appdevncsu.gpai.viewmodel.HomeViewModel
import org.appdevncsu.gpai.viewmodel.TranscriptRepository
import org.koin.androidx.compose.koinViewModel

val gradeOptions =
    listOf(
        "A+",
        "A",
        "A-",
        "B+",
        "B",
        "B-",
        "C+",
        "C",
        "C-",
        "D+",
        "D",
        "D-",
        "F",
        // These grades don't count towards GPA:
        "W", // Withdrawal
        "S", "U", // Pass/fail course grades
        "AU", "NR", // Course audit grades
        "CR", // Transfer credit
        "IN", "LA", // Temporarily incomplete
    )

@Composable
fun ForecasterScreen() {
    val homeViewModel: HomeViewModel = viewModel()
    val viewModel: TranscriptRepository = koinViewModel()
    val transcript = viewModel.transcript.collectAsState()

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

    LaunchedEffect(tempTranscript.terms.isNotEmpty()) {
        tempTranscript.terms.lastOrNull()?.let { homeViewModel.expand(it.id) }
    }
    var editingCourse by remember { mutableStateOf<CourseEditState?>(null) }
    var courseToDelete by remember { mutableStateOf<CourseDeleteState?>(null) }
    var showAddCourseDialog by remember { mutableStateOf(false) }
    var addingToTermIndex by remember { mutableIntStateOf(0) }
    val hasUnsavedChanges = tempTranscript != transcript.value
    var isSaving by remember { mutableStateOf(false) }
    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GPAHeader(gpa = tempTranscript.gpa)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(tempTranscript.terms) { termIndex, term ->
                    TermSection(
                        term = term,
                        isCurrentSemester = termIndex == tempTranscript.terms.size - 1,
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

        if (editingCourse != null) {
            CourseDialog(
                dialogState = CourseDialogState(
                    course = editingCourse!!.course,
                    isEditing = true
                ),
                onDismiss = { editingCourse = null },
                onConfirm = { updatedCourse ->
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

        if (showAddCourseDialog) {
            CourseDialogWithTermSelection(
                dialogState = CourseDialogState(
                    course = Course(
                        courseCode = "",
                        courseName = "",
                        attempted = 3,
                        earned = 3,
                        points = 12.0,
                        grade = "A"
                    ),
                    availableTerms = tempTranscript.terms,
                    isEditing = false
                ),
                initialTermIndex = addingToTermIndex,
                onDismiss = { showAddCourseDialog = false },
                onConfirm = { termId, newCourse ->
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
            )
        }

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

        if (hasUnsavedChanges) {
            FloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        isSaving = true
                        try {
                            viewModel.updateTranscript(tempTranscript)
                            snackbarHostState.showSnackbar("Changes saved successfully!")
                        } catch (e: Exception) {
                            snackbarHostState.showSnackbar("Failed to save changes. Please try again.")
                        } finally {
                            isSaving = false
                        }
                    }
                },
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(16.dp),
                containerColor = BrandPurple
            ) {
                if (isSaving) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Save Changes",
                        tint = Color.White
                    )
                }
            }
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }
}

@Composable
fun GPAHeader(gpa: Double) {
    val locale = LocalContext.current.resources.configuration.locales.get(0)

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
                text = "Cumulative GPA: ${String.format(locale, "%.2f", gpa)}",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )
        }
    }
}


@Composable
fun TermSection(
    term: Term,
    isCurrentSemester: Boolean,
    onUpdateTerm: (Term) -> Unit,
    onEditCourse: (Int, Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onAddCourse: () -> Unit
) {
    val viewModel: HomeViewModel = viewModel()
    val isExpanded = viewModel.expandedTerms.collectAsState().value.contains(term.id)

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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { viewModel.toggleExpanded(term.id) },
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

            IconButton(onClick = { viewModel.toggleExpanded(term.id) }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "Collapse" else "Expand",
                    tint = BrandPurple
                )
            }
        }

        if (isExpanded) {
            Spacer(modifier = Modifier.height(12.dp))

            term.courses.forEachIndexed { courseIndex, course ->
                CourseItem(
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
fun CourseItem(
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

        var gradeExpanded by remember { mutableStateOf(false) }

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CourseDialog(
    dialogState: CourseDialogState,
    onDismiss: () -> Unit,
    onConfirm: (Course) -> Unit
) {
    var courseName by remember { mutableStateOf(dialogState.course.courseName) }
    var courseCode by remember { mutableStateOf(dialogState.course.courseCode) }
    var creditHours by remember { mutableStateOf(dialogState.course.attempted.toString()) }
    var selectedGrade by remember { mutableStateOf(dialogState.course.grade) }
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
                    text = if (dialogState.isEditing) "Edit Course" else "Add Course",
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
                                val updatedCourse = dialogState.course.copy(
                                    courseCode = courseCode,
                                    courseName = courseName,
                                    attempted = creditHours.toIntOrNull() ?: 0,
                                    earned = creditHours.toIntOrNull() ?: 0,
                                    grade = selectedGrade,
                                    points = calculatePoints(
                                        creditHours.toIntOrNull() ?: 0,
                                        selectedGrade
                                    )
                                )
                                onConfirm(updatedCourse)
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
    dialogState: CourseDialogState,
    initialTermIndex: Int,
    onDismiss: () -> Unit,
    onConfirm: (termId: Int, course: Course) -> Unit
) {
    var courseName by remember { mutableStateOf(dialogState.course.courseName) }
    var courseCode by remember { mutableStateOf(dialogState.course.courseCode) }
    var creditHours by remember { mutableStateOf(dialogState.course.attempted.toString()) }
    var selectedGrade by remember { mutableStateOf(dialogState.course.grade) }
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
                    text = if (dialogState.isEditing) "Edit Course" else "Add Course",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                ExposedDropdownMenuBox(
                    expanded = termExpanded,
                    onExpandedChange = { termExpanded = !termExpanded }
                ) {
                    TextField(
                        value = if (dialogState.availableTerms.isNotEmpty() && selectedTermIndex < dialogState.availableTerms.size)
                            dialogState.availableTerms[selectedTermIndex].name
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
                        dialogState.availableTerms.forEachIndexed { index, term ->
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
                            if (courseName.isNotBlank() && creditHours.isNotBlank() && dialogState.availableTerms.isNotEmpty()) {
                                val termId = dialogState.availableTerms[selectedTermIndex].id
                                val newCourse = dialogState.course.copy(
                                    courseCode = courseCode,
                                    courseName = courseName,
                                    attempted = creditHours.toIntOrNull() ?: 0,
                                    earned = creditHours.toIntOrNull() ?: 0,
                                    grade = selectedGrade,
                                    points = calculatePoints(
                                        creditHours.toIntOrNull() ?: 0,
                                        selectedGrade
                                    )
                                )
                                onConfirm(termId, newCourse)
                            }
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                        enabled = courseName.isNotBlank() && creditHours.isNotBlank() && dialogState.availableTerms.isNotEmpty()
                    ) {
                        Text("Save")
                    }
                }
            }
        }
    }
}

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
data class CourseEditState(
    val termIndex: Int,
    val courseIndex: Int,
    val course: Course
)

data class CourseDeleteState(
    val course: Course
)

data class CourseDialogState(
    val course: Course,
    val availableTerms: List<Term> = emptyList(),
    val isEditing: Boolean = false
)

@Preview(showBackground = true)
@Composable
fun ForecasterPreview() {
    GPAiTheme {
        ForecasterScreen()
    }
}