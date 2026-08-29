package org.appdevncsu.gpai.screen

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import coil3.compose.AsyncImage
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.ui.theme.GPAiTheme

@Composable
fun ProfileScreen(
    navController: NavHostController,
    userName: String = "",
    userEmail: String = "",
    photoURL: String? = null,
    isSignedIn: Boolean = false,
    onSignOut: () -> Unit = {},
) {
    ProfileScreenContent(
        userName = userName,
        userEmail = userEmail,
        photoURL = photoURL,
        isSignedIn = isSignedIn,
        onUploadNewTranscript = {
            navController.popBackStack()
            navController.navigate("intro")
        },
        onSignOut = {
            onSignOut()
            navController.navigate("forecaster") {
                popUpTo("forecaster") { inclusive = true }
                launchSingleTop = true
            }
        },
        onSignIn = { navController.navigate("advisor") },
    )
}

@Composable
private fun ProfileScreenContent(
    userName: String = "",
    userEmail: String = "",
    photoURL: String? = null,
    isSignedIn: Boolean = false,
    onUploadNewTranscript: () -> Unit = {},
    onSignOut: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .padding(horizontal = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (isSignedIn) {
                AsyncImage(
                    model = photoURL,
                    contentDescription = stringResource(R.string.profile_picture),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier
                        .size(96.dp)
                        .clip(CircleShape)
                )
            }

            Column(horizontalAlignment = if (isSignedIn) Alignment.Start else Alignment.CenterHorizontally) {
                Text(
                    text = if (isSignedIn) userName else stringResource(R.string.signed_out),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = if (isSignedIn) userEmail else stringResource(R.string.sign_in_to_chat),
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)) {
            if (isSignedIn) {
                Button(
                    onClick = onSignOut,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Text(stringResource(R.string.sign_out))
                }
            } else {
                Button(onClick = onSignIn) {
                    Text(stringResource(R.string.sign_in))
                }
            }

            Button(onClick = onUploadNewTranscript) {
                Text(stringResource(R.string.upload_new_transcript))
            }
        }
    }
}

@Preview(showBackground = true, name = "Signed In")
@Composable
private fun ProfileSignedInNoPhotoPreview() {
    GPAiTheme {
        ProfileScreenContent(
            userName = "John Doe",
            userEmail = "jdoe@ncsu.edu",
            photoURL = null,
            isSignedIn = true,
        )
    }
}

@Preview(showBackground = true, name = "Signed Out")
@Composable
private fun ProfileSignedOutPreview() {
    GPAiTheme {
        ProfileScreenContent(
            userName = "",
            userEmail = "",
            photoURL = null,
            isSignedIn = false,
        )
    }
}
