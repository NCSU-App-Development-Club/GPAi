package org.appdevncsu.gpai.screen.advisor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Flag
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.appdevncsu.gpai.R

@Composable
fun FlagButton(
    isFlagged: Boolean,
    onFlag: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (isFlagged) {
        val flaggedDesc = stringResource(R.string.report_already_flagged)
        Icon(
            imageVector = Icons.Outlined.Flag,
            contentDescription = flaggedDesc,
            tint = MaterialTheme.colorScheme.error,
            modifier = modifier
                .size(24.dp)
                .semantics {
                    contentDescription = flaggedDesc
                }
        )
    } else {
        val reportDesc = stringResource(R.string.report_message_description)
        IconButton(
            onClick = onFlag,
            modifier = modifier.size(32.dp),
        ) {
            Icon(
                imageVector = Icons.Outlined.Flag,
                contentDescription = reportDesc,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .size(24.dp)
                    .semantics {
                        contentDescription = reportDesc
                        role = Role.Button
                    }
            )
        }
    }
}

@Composable
fun FlagReasonDialog(
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    val reasons = listOf(
        stringResource(R.string.report_reason_inaccurate),
        stringResource(R.string.report_reason_offensive),
        stringResource(R.string.report_reason_misleading),
        stringResource(R.string.report_reason_other),
    )
    var selectedReason by remember { mutableStateOf<String?>(null) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.report_reason_title)) },
        text = {
            Column {
                reasons.forEach { reason ->
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp)
                    ) {
                        RadioButton(
                            selected = selectedReason == reason,
                            onClick = { selectedReason = reason }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = reason,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { selectedReason?.let { onConfirm(it) } },
                enabled = selectedReason != null
            ) {
                Text(stringResource(R.string.report_message))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
private fun PreviewFlagButton() {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(16.dp)
    ) {
        FlagButton(isFlagged = false, onFlag = {})
        Spacer(modifier = Modifier.width(16.dp))
        FlagButton(isFlagged = true, onFlag = {})
    }
}
