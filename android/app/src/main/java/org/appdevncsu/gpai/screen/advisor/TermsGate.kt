package org.appdevncsu.gpai.screen.advisor

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
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
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.security.PreferencesManager
import org.koin.compose.koinInject

@Composable
fun TermsGate(
    modifier: Modifier = Modifier,
    child: @Composable () -> Unit
) {
    val preferencesManager: PreferencesManager = koinInject()
    var hasAccepted by remember { mutableStateOf(preferencesManager.hasAcceptedTerms) }

    if (hasAccepted) {
        child()
    } else {
        TermsGateContent(
            onAccept = {
                preferencesManager.hasAcceptedTerms = true
                hasAccepted = true
            },
            modifier = modifier,
        )
    }
}

@Composable
private fun TermsGateContent(
    onAccept: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val uriHandler = LocalUriHandler.current

    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.terms_gate_title),
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = stringResource(R.string.terms_gate_message),
            fontSize = 14.sp,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        TextButton(onClick = { uriHandler.openUri("https://gpai.appdevncsu.org/terms") }) {
            Text(stringResource(R.string.terms_gate_read))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = onAccept,
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text(stringResource(R.string.terms_accept))
        }
    }
}
