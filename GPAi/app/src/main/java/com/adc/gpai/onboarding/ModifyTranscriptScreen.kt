package com.adc.gpai.onboarding

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.adc.gpai.home.HomeActivity
import com.adc.gpai.ui.theme.GPAiTheme

@Composable
fun ModifyTranscriptScreen(navController: NavHostController? = null) {
    val context = LocalContext.current
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("modify_screen")
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        Text(text = "Course List")
        Button(onClick = { context.startActivity(Intent(context, HomeActivity::class.java)) }) {
            Text(text = "Finish")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun ModifyTranscriptPreview() {
    GPAiTheme {
        ModifyTranscriptScreen()
    }
}