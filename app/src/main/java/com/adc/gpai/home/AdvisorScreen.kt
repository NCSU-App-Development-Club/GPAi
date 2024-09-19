package com.adc.gpai.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.tooling.preview.Preview
import com.adc.gpai.ui.theme.GPAiTheme

@Composable
fun AdvisorScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("advisor_screen")
        ,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Advisor")
    }
}

@Preview(showBackground = true)
@Composable
fun AdvisorPreview() {
    GPAiTheme {
        AdvisorScreen()
    }
}