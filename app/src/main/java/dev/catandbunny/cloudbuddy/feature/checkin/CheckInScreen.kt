package dev.catandbunny.cloudbuddy.feature.checkin

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.UserMood
import dev.catandbunny.cloudbuddy.ui.component.CloudTopBar

@Composable
fun CheckInScreen(
    buddyName: String,
    onBack: () -> Unit,
    onSave: (UserMood, String) -> Unit,
) {
    var selected by remember { mutableStateOf<UserMood?>(null) }
    var note by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize()) {
        CloudTopBar("Небольшая пауза", onBack)
        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Text("Как ты сейчас?", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Здесь нет правильного ответа. ${buddyName} подстроит атмосферу под тебя.", modifier = Modifier.padding(top = 6.dp, bottom = 18.dp))
            UserMood.entries.chunked(2).forEach { row ->
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    row.forEach { mood ->
                        Card(
                            modifier = Modifier.weight(1f).padding(vertical = 5.dp).clickable { selected = mood },
                            shape = RoundedCornerShape(22.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = if (selected == mood) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                            ),
                        ) {
                            Column(Modifier.padding(16.dp)) {
                                Text(mood.emoji, style = MaterialTheme.typography.headlineMedium)
                                Text(mood.title, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }
            }
            OutlinedTextField(
                value = note,
                onValueChange = { note = it.take(240) },
                modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
                label = { Text("Что повлияло? Можно пропустить") },
                minLines = 3,
                supportingText = { Text("${note.length}/240") },
            )
            Button(
                onClick = { selected?.let { onSave(it, note) } },
                modifier = Modifier.fillMaxWidth().padding(vertical = 24.dp),
                enabled = selected != null,
            ) { Text("Сохранить погоду") }
        }
    }
}
