package org.appdevncsu.gpai.screen

import androidx.compose.animation.core.animateDp
import androidx.compose.animation.core.updateTransition
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import org.appdevncsu.gpai.ui.theme.BrandDarkPurple
import org.appdevncsu.gpai.ui.theme.BrandPurple
import org.appdevncsu.gpai.viewmodel.TranscriptRepository
import org.koin.androidx.compose.koinViewModel

/**
 * A composable function that manages the navigation between different screens (Forecaster and Advisor)
 * based on the current state of the home screen.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GPAiAppBar(navController: NavHostController) {
    TopAppBar(
        title = {
            Text(
                text = "GPAi",
                fontWeight = FontWeight.Black,
                fontSize = 40.sp,
                textAlign = TextAlign.Center
            )
        },
        actions = {
            IconButton(
                onClick = {
                    // Send the user back to the onboarding screen
                    navController.navigate("intro")
                }
            ) {
                Icon(
                    imageVector = Icons.Default.Refresh,
                    contentDescription = "Upload New Transcript",
                )
            }
        },
        modifier = Modifier.padding(8.dp)
    )
}

/**
 * A composable function for the toggle button at the bottom of the screen that allows switching
 * between the Forecaster and Advisor views.
 *
 * @param currentRoute The current route, which determines the toggle's position.
 * @param navController The navigation controller used to navigate between composables based on the toggle.
 * @param modifier Modifier to apply to the toggle button container.
 */
@Composable
fun HomeViewToggle(
    currentRoute: String,
    navController: NavController,
    modifier: Modifier = Modifier
) {
    val transition = updateTransition(currentRoute, label = "state transition")

    val offsetX by transition.animateDp(label = "offset animation") { state ->
        when (state) {
            "forecaster" -> 0.dp
            "advisor" -> (LocalConfiguration.current.screenWidthDp.dp / 2) - 16.dp
            else -> 0.dp
        }
    }

    val commonModifier = Modifier.fillMaxSize()
    val boxWidth = LocalConfiguration.current.screenWidthDp.dp / 2 - 16.dp

    Box(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(BrandDarkPurple)
            .height(75.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .offset(x = offsetX)
                .clip(RoundedCornerShape(12.dp))
                .background(BrandPurple)
                .fillMaxHeight()
                .width(boxWidth)
        ) {}

        Row(modifier = Modifier.fillMaxSize()) {
            ToggleButton(
                text = "Forecaster",
                modifier = commonModifier
                    .weight(1f)
                    .testTag("forecaster"),
                enabled = currentRoute != "forecaster",
                onClick = {
                    navController.navigate("forecaster") {
                        // Replace the current entry in the back stack when switching between forecaster and advisor screens
                        popUpTo(navController.currentDestination!!.id) { inclusive = true }
                    }
                }
            )
            ToggleButton(
                text = "Advisor",
                modifier = commonModifier
                    .weight(1f)
                    .testTag("advisor"),
                enabled = currentRoute != "advisor",
                onClick = {
                    navController.navigate("advisor") {
                        // Replace the current entry in the back stack when switching between forecaster and advisor screens
                        popUpTo(navController.currentDestination!!.id) { inclusive = true }
                    }
                }
            )
        }
    }
}

/**
 * A composable function representing a toggle button that switches between different views
 * (Forecaster or Advisor) and updates the navigation state.
 *
 * @param text The text label of the button.
 * @param modifier Modifier to apply to the toggle button.
 * @param enabled Whether the button should respond to clicks
 */
@Composable
fun ToggleButton(
    text: String,
    enabled: Boolean,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
) {
    // Button container with clickable modifier to switch state
    Box(
        contentAlignment = Alignment.Center,
        modifier = modifier
            .fillMaxSize()
            .clickable {
                if (enabled) {
                    onClick()
                }
            }
    ) {
        // Text label inside the toggle button
        Text(
            text,
            color = Color.White,
            fontWeight = if (!enabled) FontWeight.Black else FontWeight.Normal,
            fontSize = 20.sp
        )
    }
}
