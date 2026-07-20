package dev.catandbunny.cloudbuddy.feature.chat

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudWeather
import dev.catandbunny.cloudbuddy.ui.component.CloudTopBar

@Composable
fun ChatScreen(
    buddyName: String,
    weather: CloudWeather,
    messages: List<ChatMessage>,
    isSending: Boolean,
    onBack: () -> Unit,
    onSend: (String) -> Unit,
) {
    var input by remember { mutableStateOf("") }
    val listState = rememberLazyListState()
    LaunchedEffect(messages.size) {
        if (messages.isNotEmpty()) listState.animateScrollToItem(messages.lastIndex)
    }
    Column(Modifier.fillMaxSize().imePadding()) {
        CloudTopBar("Разговор с $buddyName", onBack)
        Text(
            "Я ИИ-компаньон, не врач. В экстренной ситуации обратись к человеку или местной службе помощи.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        LazyColumn(
            state = listState,
            modifier = Modifier.weight(1f).fillMaxWidth().padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(messages, key = { it.id }) { message ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (message.fromUser) Arrangement.End else Arrangement.Start,
                ) {
                    Card(
                        modifier = Modifier.fillMaxWidth(.84f),
                        shape = RoundedCornerShape(22.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = if (message.fromUser) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                        ),
                    ) { Text(message.text, Modifier.padding(15.dp)) }
                }
            }
            if (isSending) {
                item { CircularProgressIndicator(Modifier.padding(16.dp)) }
            }
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            OutlinedTextField(
                value = input,
                onValueChange = { input = it.take(1_000) },
                modifier = Modifier.weight(1f),
                placeholder = { Text("Напиши, что происходит…") },
                maxLines = 5,
            )
            IconButton(
                onClick = {
                    onSend(input)
                    input = ""
                },
                enabled = input.isNotBlank() && !isSending,
            ) { Text("➤", style = MaterialTheme.typography.headlineSmall) }
        }
    }
}
