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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import kotlinx.coroutines.launch
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.activity.scopedKoinViewModel
import org.appdevncsu.gpai.activity.scopedViewModel
import org.appdevncsu.gpai.models.Course
import org.appdevncsu.gpai.models.Term
import org.appdevncsu.gpai.models.Transcript
import org.appdevncsu.gpai.ui.theme.BrandDarkPurple
import org.appdevncsu.gpai.ui.theme.BrandPurple
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.appdevncsu.gpai.viewmodel.HomeViewModel
import org.appdevncsu.gpai.viewmodel.TranscriptRepository
import androidx.compose.ui.platform.LocalConfiguration

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
fun ForecasterScreen(navController: NavHostController) {
    val homeViewModel: HomeViewModel = scopedViewModel(navController)
    val viewModel: TranscriptRepository = scopedKoinViewModel(navController)
    val transcript = viewModel.transcript.collectAsState()

    var tempTranscript by remember {
        mutableStateOf(
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
    val changesSavedMessage = stringResource(R.string.changes_saved)
    val changesFailedMessage = stringResource(R.string.changes_failed)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            GPAHeader(gpa = tempTranscript.displayGpa)
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(tempTranscript.terms) { termIndex, term ->
                    TermSection(
                        viewModel = homeViewModel,
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
                title = { Text(stringResource(R.string.confirm_deletion)) },
                text = { Text(stringResource(R.string.delete_course_confirm, courseToDelete!!.course.courseName)) },
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
                        Text(stringResource(R.string.delete))
                    }
                },
                dismissButton = {
                    TextButton(onClick = { courseToDelete = null }) {
                        Text(stringResource(R.string.cancel))
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
                            snackbarHostState.showSnackbar(changesSavedMessage)
                        } catch (e: Exception) {
                            e.printStackTrace()
                            snackbarHostState.showSnackbar(changesFailedMessage)
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
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp
                    )
                } else {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = stringResource(R.string.save_changes),
                        tint = MaterialTheme.colorScheme.onPrimary
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
                text = stringResource(R.string.cumulative_gpa, gpa),
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimary,
                maxLines = 1
            )
        }
    }
}


@Composable
fun TermSection(
    viewModel: HomeViewModel,
    term: Term,
    isCurrentSemester: Boolean,
    onUpdateTerm: (Term) -> Unit,
    onEditCourse: (Int, Course) -> Unit,
    onDeleteCourse: (Course) -> Unit,
    onAddCourse: () -> Unit
) {
    val isExpanded = viewModel.expandedTerms.collectAsState().value.contains(term.id)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, MaterialTheme.colorScheme.outlineVariant, RoundedCornerShape(8.dp))
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
                .semantics(mergeDescendants = true) {}
                .clickable { viewModel.toggleExpanded(term.id) },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = term.name + if (isCurrentSemester) " " + stringResource(R.string.current_semester_suffix) else "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCurrentSemester) BrandPurple else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1
                )
                Text(
                    text = stringResource(R.string.semester_gpa, term.displayGpa),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = { viewModel.toggleExpanded(term.id) }) {
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) stringResource(R.string.collapse) else stringResource(R.string.expand),
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
                    contentDescription = stringResource(R.string.add_course)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(text = stringResource(R.string.add_course))
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
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f), RoundedCornerShape(8.dp))
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
                text = stringResource(R.string.credits_format, course.attempted),
                fontSize = 12.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant
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
                    color = MaterialTheme.colorScheme.onPrimary,
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
                contentDescription = stringResource(R.string.edit_course),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(20.dp)
            )
        }

        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.delete_course),
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
                    text = if (dialogState.isEditing) stringResource(R.string.edit_course) else stringResource(R.string.add_course),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                TextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text(stringResource(R.string.course_code_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text(stringResource(R.string.course_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = creditHours,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            creditHours = it
                        }
                    },
                    label = { Text(stringResource(R.string.credit_hours_label)) },
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
                        label = { Text(stringResource(R.string.grade_label)) },
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
                        Text(stringResource(R.string.cancel))
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
                        Text(stringResource(R.string.save))
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
                    text = if (dialogState.isEditing) stringResource(R.string.edit_course) else stringResource(R.string.add_course),
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
                        else stringResource(R.string.select_term),
                        onValueChange = {},
                        readOnly = true,
                        label = { Text(stringResource(R.string.term_label)) },
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
                    label = { Text(stringResource(R.string.course_code_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text(stringResource(R.string.course_name_label)) },
                    modifier = Modifier.fillMaxWidth()
                )

                TextField(
                    value = creditHours,
                    onValueChange = {
                        if (it.isEmpty() || it.all { char -> char.isDigit() }) {
                            creditHours = it
                        }
                    },
                    label = { Text(stringResource(R.string.credit_hours_label)) },
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
                        label = { Text(stringResource(R.string.grade_label)) },
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
                        Text(stringResource(R.string.cancel))
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
                        Text(stringResource(R.string.save))
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
    val navController = rememberNavController()
    GPAiTheme {
        ForecasterScreen(navController)
    }
}