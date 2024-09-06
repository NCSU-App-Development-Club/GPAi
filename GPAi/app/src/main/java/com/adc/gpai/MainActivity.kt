package com.adc.gpai

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.adc.gpai.models.Course
import com.adc.gpai.models.Term
import com.adc.gpai.models.Transcript
import com.adc.gpai.ui.theme.GPAiTheme
import com.adc.gpai.utils.Utils
import com.tom_roush.pdfbox.android.PDFBoxResourceLoader
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            var transcript = remember {
                mutableStateOf<Transcript?>(null)
            }
            GPAiTheme {
                Column(
                    modifier = Modifier
                        .fillMaxSize(),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Spacer(modifier = Modifier.weight(1f))
                        Text(text = "Please upload your transcript")
                        RequestFileButton( onFileSelected = {fileUri ->
                            val pdfText = Utils.readTextFromPdf(this@MainActivity, fileUri)
                            transcript.value = Utils.parseTranscript(pdfText!!)
                        })
                    }
                    CourseList(
                        transcript = transcript.value, modifier = Modifier
                            .padding(bottom = 8.dp)
                            .weight(1f)
                    )

                }

            }
        }
    }
}

@Composable
fun RequestFileButton(
    modifier: Modifier = Modifier,
    onFileSelected: (Uri) -> Unit
) {
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            uri?.let { onFileSelected(it) }
        }
    )

    Button(modifier = modifier, onClick = { launcher.launch("*/*") }) {
        Text("Select File")
    }
}

@Composable
fun CourseList(transcript: Transcript?, modifier: Modifier = Modifier) {
    if (transcript != null) {
        Column(modifier = modifier.verticalScroll(rememberScrollState())) {
            for (term in transcript.terms) {
                Text(
                    text = term.name,
                    style = MaterialTheme.typography.headlineLarge
                )
                for (course in term.courses) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = course.courseCode, fontWeight = FontWeight.Bold)
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
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    } else {
        Text(text = "No transcript uploaded", modifier = modifier)
    }
}

// Sample data for preview
val sampleTranscript = Transcript(
    listOf(
        Term(
            "Fall 2023",
            listOf(
                Course("Fall 2023", "CS 101", "Introduction to Programming", 3, 3, 12.0, "A"),
                Course("Fall 2023", "MA 200", "Calculus I", 3, 3, 9.0, "B+"),
            )
        ),
        Term(
            "Spring2024",
            listOf(
                Course("Spring 2024", "CS 201", "Data Structures", 4, 4, 14.0, "A-"),
                Course("Spring 2024", "PH 101", "Physics I", 3, 3, 9.0, "B")
            )
        )
    )
)

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    GPAiTheme {
//       RequestFileButton()
    }
}