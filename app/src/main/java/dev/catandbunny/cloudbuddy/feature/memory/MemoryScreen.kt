package dev.catandbunny.cloudbuddy.feature.memory

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.ui.component.CloudTopBar

@Composable
fun MemoryScreen(
    memories: List<String>,
    enabled: Boolean,
    onBack: () -> Unit,
    onClear: () -> Unit,
) {
    var confirmClear by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        CloudTopBar(
            title = "Память облачка",
            onBack = onBack,
            action = {
                if (memories.isNotEmpty()) TextButton(onClick = { confirmClear = true }) { Text("Очистить") }
            },
        )
        Text(
            if (enabled) "Эти записи хранятся только на устройстве и не отправляются локальному собеседнику." else "Память отключена. Старые записи остаются, пока ты их не удалишь.",
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        if (memories.isEmpty()) {
            Column(
                modifier = Modifier.weight(1f).fillMaxWidth().padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text("Пока здесь тихо ☁️", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                Text("Записи появятся, если ты добавишь комментарий во время check-in.", modifier = Modifier.padding(top = 8.dp))
            }
        } else {
            LazyColumn(Modifier.weight(1f).padding(horizontal = 16.dp)) {
                itemsIndexed(memories) { index, memory ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        shape = RoundedCornerShape(20.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Column(Modifier.padding(16.dp)) {
                            Text("Момент ${memories.size - index}", style = MaterialTheme.typography.labelMedium)
                            Text(memory, modifier = Modifier.padding(top = 4.dp))
                        }
                    }
                }
            }
        }
    }
    if (confirmClear) {
        AlertDialog(
            onDismissRequest = { confirmClear = false },
            title = { Text("Очистить память?") },
            text = { Text("Все сохранённые заметки будут удалены с устройства.") },
            confirmButton = {
                TextButton(onClick = { onClear(); confirmClear = false }) { Text("Очистить") }
            },
            dismissButton = { TextButton(onClick = { confirmClear = false }) { Text("Отмена") } },
        )
    }
}
