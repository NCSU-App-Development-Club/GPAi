package com.adc.gpai.onboarding

import android.content.Intent
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.adc.gpai.R
import com.adc.gpai.home.HomeActivity
import com.adc.gpai.ui.theme.BrandDarkPurple
import com.adc.gpai.ui.theme.BrandFailureRed
import com.adc.gpai.ui.theme.BrandPurple
import com.adc.gpai.ui.theme.BrandSuccessGreen
import com.adc.gpai.ui.theme.GPAiTheme
import com.adc.gpai.utils.PDFUtils
import org.koin.androidx.compose.koinViewModel

/**
 * Enum representing the current state of the file upload process.
 */
enum class UploadState {
    IDLE, SUCCESS, ERROR
}

/**
 * Screen for uploading a transcript.
 *
 * This activity contains the UI and logic
 * for uploading and parsing a transcript PDF.
 */
@Composable
fun UploadTranscriptScreen(navController: NavHostController? = null) {

    val viewModel: TranscriptRepository = koinViewModel()

    // State to track the upload process: IDLE, SUCCESS, or ERROR.
    var uploadState = remember { mutableStateOf(UploadState.IDLE) }

    // Apply the custom theme to the UI.
    val context = LocalContext.current
    GPAiTheme {
        Column(
            modifier = Modifier
                .fillMaxSize() // Fill the entire screen.
                .padding(16.dp)
                .testTag("upload_screen"),
            verticalArrangement = Arrangement.Center, // Center content vertically.
            horizontalAlignment = Alignment.CenterHorizontally // Center content horizontally.
        ) {
            Column(
                modifier = Modifier.weight(1f), // Weighting the column to fill remaining space.
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Spacer(modifier = Modifier.weight(1f)) // Spacer to add empty space above.
                Text(text = "Please upload your transcript")

                // Button to request a file upload from the user.
                RequestFileButton(
                    modifier = Modifier
                        .padding(vertical = 16.dp), // Add padding around the button.
                    buttonState = uploadState,
                    onFileSelected = { fileUri ->
                        // Read text from the selected PDF file.
                        val pdfText = PDFUtils.readTextFromPdf(context, fileUri)

                        // If file reading fails, set the state to ERROR.
                        if (pdfText == null) {
                            uploadState.value = UploadState.ERROR
                        } else {
                            // Parse the transcript from the PDF content.
                            val transcript = PDFUtils.parseTranscript(pdfText)

                            // Check if parsing was successful and update the state accordingly.
                            if (transcript.terms.isEmpty()) {
                                uploadState.value = UploadState.ERROR
                            } else {
                                uploadState.value = UploadState.SUCCESS
                                viewModel.updateTranscript(transcript)
                            }
                        }
                    })

                Button(
                    onClick = {
                        context.startActivity(Intent(context, HomeActivity::class.java))
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = BrandDarkPurple),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(75.dp)
                ) {
                    Text(text = "Next", fontSize = 40.sp)
                }
            }
        }
    }
}

/**
 * Composable function to render a button that allows the user to select and upload a file.
 *
 * @param modifier Modifier to apply custom styling.
 * @param buttonState Mutable state holding the current upload state (IDLE, SUCCESS, or ERROR).
 * @param onFileSelected Callback invoked when a file is selected by the user.
 */
@Composable
fun RequestFileButton(
    modifier: Modifier = Modifier,
    buttonState: MutableState<UploadState> = mutableStateOf(UploadState.IDLE),
    onFileSelected: (Uri) -> Unit
) {
    // Launcher for the file selection activity. Opens a file picker when the button is clicked.
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent(),
        onResult = { uri: Uri? ->
            // If a file is selected, trigger the onFileSelected callback.
            uri?.let { onFileSelected(it) }
        }
    )

    // Change the button's color based on the current state.
    val buttonColor = when (buttonState.value) {
        UploadState.IDLE -> BrandPurple
        UploadState.SUCCESS -> BrandSuccessGreen
        UploadState.ERROR -> BrandFailureRed
    }

    // Set different icons for each state.
    val buttonIcon = when (buttonState.value) {
        UploadState.IDLE -> R.drawable.upload
        UploadState.SUCCESS -> R.drawable.check
        UploadState.ERROR -> R.drawable.error
    }

    // Display different button text based on the current upload state.
    val buttonText = when (buttonState.value) {
        UploadState.IDLE -> "Click here to upload your transcript"
        UploadState.SUCCESS -> "Click here to upload a different transcript"
        UploadState.ERROR -> "Couldn't parse transcript, please try again"
    }

    // Create the actual upload button.
    Button(
        modifier = modifier
            .fillMaxWidth() // Make the button fill the width of the screen.
            .height(300.dp), // Set the height of the button.
        shape = RoundedCornerShape(12), // Make the button corners rounded.
        colors = ButtonDefaults.buttonColors(buttonColor), // Set the background color.
        onClick = { launcher.launch("application/pdf") } // Open the file picker when clicked.
    ) {
        // Layout for the button's content: an icon and text.
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                contentDescription = null,
                modifier = Modifier
                    .size(120.dp) // Set the icon size.
                    .padding(bottom = 16.dp), // Add padding below the icon.
                painter = painterResource(id = buttonIcon)
            )
            Text(fontSize = 20.sp, textAlign = TextAlign.Center, text = buttonText) // Display the button text.
        }
    }
}

/**
 * Preview function to display the UI in the Android Studio preview window.
 */
@Preview(showBackground = true)
@Composable
fun UploadTranscriptPreview() {
    GPAiTheme {
        val navController = rememberNavController()
        UploadTranscriptScreen(navController = navController)
    }
}
