package org.appdevncsu.gpai.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
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
    onDeleteAccount: () -> Unit = {},
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
        onDeleteAccount = {
            onDeleteAccount()
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
    onDeleteAccount: () -> Unit = {},
    onSignIn: () -> Unit = {},
) {
    var showDeleteDialog by remember { mutableStateOf(false) }
    val uriHandler = LocalUriHandler.current

    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text(stringResource(R.string.delete_account_title)) },
            text = { Text(stringResource(R.string.delete_account_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    onDeleteAccount()
                }) {
                    Text(stringResource(R.string.delete_account_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            },
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .widthIn(max = 600.dp)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp),
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Profile header
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            if (isSignedIn) {
                AsyncImage(
                    model = photoURL,
                    contentDescription = stringResource(R.string.profile_picture),
                    contentScale = ContentScale.Crop,
                    error = rememberVectorPainter(Icons.Default.Person),
                    modifier = Modifier
                        .size(56.dp)
                        .clip(CircleShape)
                )
                Spacer(modifier = Modifier.width(16.dp))
            }

            Column {
                Text(
                    text = if (isSignedIn) userName else stringResource(R.string.signed_out),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.SemiBold,
                )
                if (isSignedIn) {
                    Text(
                        text = userEmail,
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.sign_in_to_chat),
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Account actions
        if (isSignedIn) {
            SettingsRow(
                icon = Icons.Default.FileUpload,
                label = stringResource(R.string.upload_new_transcript),
                onClick = onUploadNewTranscript,
            )
            SettingsRow(
                icon = Icons.AutoMirrored.Filled.Logout,
                label = stringResource(R.string.sign_out),
                onClick = onSignOut,
            )
        } else {
            SettingsRow(
                icon = Icons.Default.Person,
                label = stringResource(R.string.sign_in),
                onClick = onSignIn,
            )
        }

        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        // Legal
        SettingsRow(
            icon = Icons.Default.Security,
            label = stringResource(R.string.privacy_policy),
            onClick = { uriHandler.openUri("https://gpai.appdevncsu.org/privacy") },
        )
        SettingsRow(
            icon = Icons.Default.Description,
            label = stringResource(R.string.terms_of_service),
            onClick = { uriHandler.openUri("https://gpai.appdevncsu.org/terms") },
        )

        // Danger zone
        HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp))

        SettingsRow(
            icon = Icons.Default.DeleteForever,
            label = stringResource(R.string.delete_account),
            tint = MaterialTheme.colorScheme.error,
            onClick = { showDeleteDialog = true },
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    tint: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onSurface,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(24.dp),
        )
        Spacer(modifier = Modifier.width(20.dp))
        Text(
            text = label,
            fontSize = 16.sp,
            color = tint,
        )
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
