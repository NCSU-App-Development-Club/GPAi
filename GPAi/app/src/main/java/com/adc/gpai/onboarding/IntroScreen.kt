package com.adc.gpai.onboarding

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.NavHostController
import com.adc.gpai.ui.theme.GPAiTheme

@Composable
fun IntroScreen(navController: NavHostController? = null) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Introduction")
        Button(onClick = { navController?.navigate("upload") }) {
            Text(text = "Next")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun IntroPreview() {
    GPAiTheme {
        IntroScreen()
    }
}