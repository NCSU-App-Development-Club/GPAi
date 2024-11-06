package com.adc.gpai.home

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

data class Message(val sender: String, val content: String)

@Composable
fun AdvisorChatHistory(messages: List<Message>) {
    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(8.dp)
        )
        {
            items(messages) { message ->
                ChatBubble(message = message)
            }
        }
    }
}

@Composable
fun ChatBubble(message: Message) {
    val isUser = message.sender == "User"
    val alignment = if (isUser) Alignment.CenterEnd else Alignment.CenterStart

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        contentAlignment = alignment
    ) {
        Card(
            colors = CardDefaults.cardColors(
                containerColor = if (isUser) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(horizontal = 8.dp)
        ) {
            Text(
                text = "${message.sender}: ${message.content}",
                modifier = Modifier.padding(8.dp),
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewAdvisorChatHistory() {
    val testMessages = listOf(
        Message("AI", "Hey Buddy!"),
        Message("User", "Hi AI!"))

    AdvisorChatHistory(testMessages)
}