package com.adc.gpai.home

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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adc.gpai.R
import com.adc.gpai.ui.theme.GPAiTheme

@Composable
fun AdvisorScreen(viewModel: HomeViewModel) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
            .testTag("advisor_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        ChatInput(viewModel, sendText = { question -> viewModel.askQuestion(question) })
    }
}

@Composable
fun ChatInput(
    viewModel: HomeViewModel,
    sendText: (String) -> Unit = {},
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
                Text("Speech Recognition Failed")
            },
            text = {
                Text("Your device does not support speech recognition natively. You may be able to use dictation through your keyboard app for a similar experience.")
            },
            confirmButton = {
                TextButton(onClick = { speechRecognitionError = false }) { Text("Dismiss") }
            })
    }

    val focusManager =
        LocalFocusManager.current // Used to unfocus the text field after pressing send

    val isLoading by viewModel.loading.collectAsState()

    TextField(
        value = input,
        placeholder = { Text("Ask for advice") },
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
                    "Microphone",
                    tint = Color.Black,
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
                    tint = Color.Black,
                    contentDescription = "Send",
                    modifier = Modifier.clickable {
                        sendText(input)
                        input = ""
                        focusManager.clearFocus()
                    }
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

@Preview(showBackground = true)
@Composable
fun AdvisorPreview() {
    GPAiTheme {
        AdvisorScreen(viewModel = HomeViewModel())
    }
}