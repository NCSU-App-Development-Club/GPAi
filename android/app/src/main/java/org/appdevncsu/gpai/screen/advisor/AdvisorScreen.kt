package org.appdevncsu.gpai.screen.advisor

import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.speech.RecognizerIntent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import kotlinx.coroutines.flow.Flow
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.api.models.Message
import org.appdevncsu.gpai.activity.scopedKoinViewModel
import org.appdevncsu.gpai.activity.scopedViewModel
import org.appdevncsu.gpai.ui.theme.GPAiTheme
import org.appdevncsu.gpai.util.LocalSnackbarRunner
import org.appdevncsu.gpai.viewmodel.HomeViewModel
import org.appdevncsu.gpai.viewmodel.TranscriptRepository

@Composable
fun AdvisorScreen(navController: NavHostController) {
    TermsGate {
        AuthGate(navController) {
            AuthenticatedAdvisorScreen(navController)
        }
    }
}

@Composable
private fun AuthenticatedAdvisorScreen(navController: NavHostController) {
    val viewModel: HomeViewModel = scopedViewModel(navController)

    val transcriptViewModel: TranscriptRepository = scopedKoinViewModel(navController)
    val transcript by transcriptViewModel.transcript.collectAsState()
    val messages by viewModel.messages.collectAsState()
    val error by viewModel.error.collectAsState()
    val pendingRetry by viewModel.pendingRetry.collectAsState()
    val canRetry = pendingRetry != null
    val isLoading by viewModel.loading.collectAsState()

    LaunchedEffect(transcript) {
        if (transcript == null || transcript?.terms?.isEmpty() == true) {
            viewModel.setContext("The user has not submitted a transcript yet. If they ask about their courses or grades, ask them to upload their transcript.")
            return@LaunchedEffect
        }
        viewModel.setContext("""
            Here is the user's current transcript:
            
            ${transcript!!.terms.map { term -> """<term>
                Name: ${term.name}
                GPA: ${term.displayGpa}
                Courses: ${term.courses.map { course -> """
                    - ${course.courseCode} (${course.courseName}): ${course.grade} (${course.points} grade points from ${course.earned} units)
                """.trimIndent() }}
            </term>""".trimIndent() }}
        """.trimIndent())
    }

    AdvisorChatContent(
        messages = messages,
        error = error,
        canRetry = canRetry,
        isLoading = isLoading,
        onRetry = { viewModel.retry() },
        onSendQuestion = { viewModel.askQuestion(it) },
        onClearMessages = { viewModel.clearMessages() },
        onFlagMessage = { messageId, reason -> viewModel.flagMessage(messageId, reason) },
        flagResultFlow = viewModel.flagResult,
    )
}

@Composable
private fun AdvisorChatContent(
    messages: List<Message>,
    error: String?,
    canRetry: Boolean,
    isLoading: Boolean,
    onRetry: () -> Unit,
    onSendQuestion: (String) -> Unit,
    onClearMessages: () -> Unit,
    onFlagMessage: (String, String) -> Unit,
    flagResultFlow: Flow<Boolean>,
    modifier: Modifier = Modifier
) {
    var showClearDialog by remember { mutableStateOf(false) }
    val snackbarRunner = LocalSnackbarRunner.current
    val successMessage = stringResource(R.string.report_submitted)
    val failureMessage = stringResource(R.string.report_failed)

    LaunchedEffect(Unit) {
        flagResultFlow.collect { success ->
            val message = if (success) successMessage else failureMessage
            snackbarRunner.send(message)
        }
    }

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.clear_conversation)) },
            text = { Text(stringResource(R.string.clear_conversation_confirm)) },
            confirmButton = {
                TextButton(onClick = {
                    showClearDialog = false
                    onClearMessages()
                }) {
                    Text(stringResource(R.string.delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = modifier,
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .testTag("advisor_screen"),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            val visibleMessages = messages.filter { it.role != "system" && !it.isContext }
            AdvisorChatHistory(
                messages = visibleMessages,
                modifier = Modifier.weight(1f),
                showClearButton = visibleMessages.any { it.role == "assistant" },
                onClearConversation = { showClearDialog = true },
                onFlagMessage = onFlagMessage,
            )
            if (error != null) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 8.dp)
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        modifier = Modifier.fillMaxWidth()
                    )
                    if (canRetry) {
                        TextButton(onClick = onRetry) {
                            Text(stringResource(R.string.retry))
                        }
                    }
                }
            }
            ChatInput(isLoading = isLoading, onSend = onSendQuestion)
        }
    }
}

@Composable
fun ChatInput(
    isLoading: Boolean,
    onSend: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var input by remember { mutableStateOf("") }

    val launcher =
        rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            // After speech recognition completes, append the result to the input's text
            if (it.resultCode == Activity.RESULT_OK) {
                val results = it.data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                    ?: return@rememberLauncherForActivityResult
                input = (input.trim() + " " + results.firstOrNull().orEmpty()).trim()
            }
        }

    var speechRecognitionError by remember { mutableStateOf(false) }

    if (speechRecognitionError) {
        AlertDialog(
            onDismissRequest = { speechRecognitionError = false },
            title = {
                Text(stringResource(R.string.speech_recognition_failed))
            },
            text = {
                Text(stringResource(R.string.speech_recognition_error))
            },
            confirmButton = {
                TextButton(onClick = { speechRecognitionError = false }) { Text(stringResource(R.string.dismiss)) }
            })
    }

    val focusManager =
        LocalFocusManager.current // Used to unfocus the text field after pressing send

    TextField(
        value = input,
        placeholder = { Text(stringResource(R.string.ask_for_advice)) },
        onValueChange = { newValue -> input = newValue },
        trailingIcon = {
            if (isLoading) {
                CircularProgressIndicator(modifier = Modifier.padding(8.dp))
                return@TextField
            }

            Row(
                modifier = Modifier.padding(horizontal = 14.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.microphone),
                    stringResource(R.string.microphone),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.clickable {
                        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH)
                        intent.putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        try {
                            launcher.launch(intent)
                        } catch (_: ActivityNotFoundException) {
                            // There are no apps installed that can provide speech recognition functionality
                            speechRecognitionError = true
                        }
                    }
                )
                Icon(
                    imageVector = Icons.AutoMirrored.Default.Send,
                    tint = if (input.isBlank()) MaterialTheme.colorScheme.onSurfaceVariant else MaterialTheme.colorScheme.primary,
                    contentDescription = stringResource(R.string.send),
                    modifier = Modifier.then(
                        if (input.isNotBlank()) Modifier.clickable {
                            onSend(input)
                            input = ""
                            focusManager.clearFocus()
                        } else Modifier
                    )
                )
            }
        },
        enabled = !isLoading,
        modifier = modifier
            .fillMaxWidth()
            .padding(start = 0.dp, top = 0.dp, end = 0.dp, bottom = 4.dp),
        // Make the text field completely rounded and remove the bottom border ("indicator")
        colors = TextFieldDefaults.colors().copy(
            unfocusedIndicatorColor = Color.Transparent,
            focusedIndicatorColor = Color.Transparent,
            disabledIndicatorColor = Color.Transparent
        ),
        shape = RoundedCornerShape(9999.dp)
    )
}

private val previewMessages = listOf(
    Message(role = "user", content = "Hi AI!"),
    Message(role = "assistant", content = "Hey Buddy!! How can I help you today?"),
    Message(role = "user", content = "What's my GPA?"),
    Message(role = "assistant", content = "Based on your transcript, your cumulative GPA is 3.80.")
)

@Preview(showBackground = true)
@Composable
fun AdvisorPreview() {
    GPAiTheme {
        AdvisorChatContent(
            messages = previewMessages,
            error = null,
            canRetry = false,
            isLoading = false,
            onRetry = {},
            onSendQuestion = {},
            onClearMessages = {},
            onFlagMessage = { _, _ -> },
            flagResultFlow = kotlinx.coroutines.flow.emptyFlow<Boolean>(),
        )
    }
}

@Preview(showBackground = true, name = "Advisor (error state)")
@Composable
fun AdvisorErrorPreview() {
    GPAiTheme {
        AdvisorChatContent(
            messages = previewMessages,
            error = "Something went wrong while getting a response. Please try again.",
            canRetry = true,
            isLoading = false,
            onRetry = {},
            onSendQuestion = {},
            onClearMessages = {},
            onFlagMessage = { _, _ -> },
            flagResultFlow = kotlinx.coroutines.flow.emptyFlow<Boolean>(),
        )
    }
}
