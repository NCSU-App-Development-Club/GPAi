package org.appdevncsu.gpai.screen.advisor

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import org.appdevncsu.gpai.R
import org.appdevncsu.gpai.api.models.Message

@Composable
fun AdvisorChatHistory(
    messages: List<Message>,
    modifier: Modifier = Modifier,
    showClearButton: Boolean = false,
    onClearConversation: () -> Unit = {},
    onFlagMessage: (messageId: String, reason: String) -> Unit = { _, _ -> },
) {
    val listState = rememberLazyListState()
    var flagTargetMessage by remember { mutableStateOf<Message?>(null) }

    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) {
            listState.animateScrollToItem(messages.lastIndex)
        }
    }

    if (flagTargetMessage != null) {
        FlagReasonDialog(
            onDismiss = { flagTargetMessage = null },
            onConfirm = { reason ->
                flagTargetMessage?.let { onFlagMessage(it.id, reason) }
                flagTargetMessage = null
            }
        )
    }

    Column(modifier = modifier.fillMaxSize()) {
        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        ) {
            items(messages) { message ->
                ChatBubble(
                    message = message,
                    onFlag = if (message.role == "assistant" && !message.isContext) {
                        { flagTargetMessage = message }
                    } else null,
                )
            }
            if (showClearButton) {
                item {
                    TextButton(onClick = onClearConversation) {
                        Text(stringResource(R.string.clear_conversation))
                    }
                }
            }
        }
    }
}

@Composable
fun ChatBubble(
    message: Message,
    onFlag: (() -> Unit)? = null,
) {
    val isUser = message.role == "user"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart
    val horizontalAlignment = if (isUser) Alignment.End else Alignment.Start

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Column(horizontalAlignment = horizontalAlignment) {
            val bubbleShape = if (isUser) {
                RoundedCornerShape(12.dp, 12.dp, 0.dp, 12.dp)
            } else {
                RoundedCornerShape(12.dp, 12.dp, 12.dp, 0.dp)
            }

            Card(
                colors = CardDefaults.cardColors(
                    containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
                ),
                shape = bubbleShape,
                modifier = Modifier.padding(horizontal = 8.dp)
            ) {
                Text(
                    text = message.content,
                    modifier = Modifier.padding(8.dp),
                    color = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSecondary
                )
            }
            if (!isUser && onFlag != null) {
                FlagButton(
                    isFlagged = message.isFlagged,
                    onFlag = onFlag,
                    modifier = Modifier.padding(start = 8.dp, top = 2.dp),
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdvisorChatHistory() {
    val testMessages = listOf(
        Message(role = "user", content = "Hi AI!"),
        Message(role = "assistant", content = "Hey Buddy!!")
    )

    AdvisorChatHistory(testMessages)
}
