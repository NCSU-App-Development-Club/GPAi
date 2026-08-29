package org.appdevncsu.gpai.screen.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.ui.theme.GPAiTheme

@Composable
fun IntroScreen(navController: NavHostController? = null) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp)
            .testTag("intro_screen"),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(modifier = Modifier.weight(0.4f))
        Image(
            painter = painterResource(id = R.drawable.a_plus_graphic),
            contentDescription = stringResource(R.string.logo_description)
        )
        Spacer(modifier = Modifier.weight(0.4f))
        Text(
            text = stringResource(R.string.intro_headline),
            fontWeight = FontWeight(weight = 900),
            fontSize = 36.sp,
            textAlign = TextAlign.Center,
            lineHeight = 44.sp,
            modifier = Modifier
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = stringResource(R.string.intro_description),
            fontWeight = FontWeight(weight = 500),
            fontSize = 18.sp,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.weight(1f))

        Button(
            modifier = Modifier
                .fillMaxWidth()
                .height(74.dp),
            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary),
            onClick = { navController?.navigate("upload") }
        ) {
            Text(
                text = stringResource(R.string.get_started),
                fontSize = 32.sp,
                fontWeight = FontWeight(weight = 700),
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Preview(showBackground = true)
@Composable
fun IntroPreview() {
    GPAiTheme {
        IntroScreen()
    }
}