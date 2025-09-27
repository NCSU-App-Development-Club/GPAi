package org.appdevncsu.gpai.onboarding

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.appdevncsu.gpai.home.HomeActivity
import org.appdevncsu.gpai.models.Course
import org.appdevncsu.gpai.models.Term
import org.appdevncsu.gpai.models.Transcript
import org.appdevncsu.gpai.ui.theme.BrandDarkPurple
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModifyTranscriptScreen() {
    val context = LocalContext.current
    val viewModel: TranscriptRepository = koinViewModel()
    
    // Observe transcript from the repository
    val transcriptState = viewModel.transcript.observeAsState()
    val transcript = transcriptState.value ?: Transcript(emptyList())
    
    // State for edit/delete operations
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
            modifier = Modifier
                .fillMaxSize()
                .testTag("modify_screen")
        ) {
            // Simple centered title
            Text(
                text = "Review Details",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 16.dp)
            )
            
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                itemsIndexed(transcript.terms) { termIndex, term ->
                    // Term section
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, Color.LightGray, RoundedCornerShape(8.dp))
                            .padding(16.dp)
                    ) {
                        Text(
                            text = term.name,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )
                        
                        // Courses in this term
                        term.courses.forEachIndexed { courseIndex, course ->
                            CourseItem(
                                course = course,
                                onEdit = {
                                    editingCourse = CourseEditState(
                                        termIndex = termIndex,
                                        courseIndex = courseIndex,
                                        course = course
                                    )
                                },
                                onDelete = {
                                    courseToDelete = CourseDeleteState(
                                        course = course
                                    )
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                        
                        // Add course button - now shown under every term
                        Button(
                            onClick = { 
                                addingToTermIndex = termIndex
                                showAddCourseDialog = true
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = 8.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add Course"
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(text = "Add a course")
                        }
                    }
                }
            }
            
            // Finish button at the bottom
            Button(
                onClick = { context.startActivity(Intent(context, HomeActivity::class.java)) },
                colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(75.dp)
            ) {
                Text(text = "Finish", fontSize = 40.sp)
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
                    
                    // Update the course with new values
                    val updatedCourse = course.copy(
                        courseCode = courseCode,
                        courseName = name,
                        attempted = hours.toIntOrNull() ?: 0,
                        earned = hours.toIntOrNull() ?: 0,
                        grade = grade,
                        points = calculatePoints(hours.toIntOrNull() ?: 0, grade)
                    )
                    
                    // Update in the repository
                    viewModel.updateCourse(updatedCourse)
                    
                    // Clear editing state
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
                terms = transcript.terms,
                initialTermIndex = addingToTermIndex,
                onDismiss = { showAddCourseDialog = false },
                onConfirm = { termId, courseCode, name, hours, grade ->
                    if (name.isNotBlank() && hours.isNotBlank()) {
                        // Create new course
                        val points = calculatePoints(hours.toIntOrNull() ?: 0, grade)
                        
                        val newCourse = Course(
                            courseCode = courseCode,
                            courseName = name,
                            attempted = hours.toIntOrNull() ?: 0,
                            earned = hours.toIntOrNull() ?: 0,
                            points = points,
                            grade = grade
                        )
                        
                        // Add to repository - now uncommented
                        viewModel.addCourse(termId, newCourse)
                        
                        // Reset dialog state
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
                            // Delete the course through the repository
                            viewModel.removeCourse(courseToDelete!!.course)
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
fun CourseItem(
    course: Course,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color.LightGray.copy(alpha = 0.2f), RoundedCornerShape(4.dp))
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        // Course name
        Text(
            text = "${course.courseCode} ${course.courseName}",
            modifier = Modifier.weight(1f)
        )
        
        // Credit hours
        Text(
            text = "${course.attempted} cr",
            modifier = Modifier.padding(horizontal = 8.dp)
        )
        
        // Grade
        Surface(
            shape = RoundedCornerShape(4.dp),
            color = when (course.grade.firstOrNull() ?: '?') {
                'A', 'S' -> Color(0xFF4CAF50) // Green
                'B' -> Color(0xFF8BC34A) // Light Green
                'C' -> Color(0xFFFFC107) // Yellow
                'D' -> Color(0xFFFF9800) // Orange
                else -> Color(0xFFF44336) // Red for F
            },
            modifier = Modifier.padding(end = 8.dp)
        ) {
            Text(
                text = course.grade,
                textAlign = TextAlign.Center,
                color = Color.White,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
        
        // Edit icon
        IconButton(onClick = onEdit) {
            Icon(
                imageVector = Icons.Outlined.Edit,
                contentDescription = "Edit Course",
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        // Delete icon
        IconButton(onClick = onDelete) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = "Delete Course",
                tint = MaterialTheme.colorScheme.error
            )
        }
    }
}

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
                
                // Course code field
                TextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Course name field
                TextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Credit hours field
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
                
                // Grade dropdown
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
                
                // Action buttons
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
                
                // Term selection dropdown
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
                
                // Course code field
                TextField(
                    value = courseCode,
                    onValueChange = { courseCode = it },
                    label = { Text("Course Code") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Course name field
                TextField(
                    value = courseName,
                    onValueChange = { courseName = it },
                    label = { Text("Course Name") },
                    modifier = Modifier.fillMaxWidth()
                )
                
                // Credit hours field
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
                
                // Grade dropdown
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
                
                // Action buttons
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

@Preview(showBackground = true)
@Composable
fun ModifyTranscriptPreview() {
    GPAiTheme {
        ModifyTranscriptScreen()
    }
}