package dev.catandbunny.cloudbuddy.feature.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
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
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.ui.component.CloudTopBar

@Composable
fun SettingsScreen(
    state: CloudBuddyState,
    onBack: () -> Unit,
    onMemory: () -> Unit,
    onSoundChanged: (Boolean) -> Unit,
    onRemindersChanged: (Boolean) -> Unit,
    onMemoryChanged: (Boolean) -> Unit,
    onReset: () -> Unit,
) {
    var confirmReset by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        CloudTopBar("Настройки", onBack)
        Column(Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp)) {
            Text("Комфорт", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 12.dp))
            SettingSwitch("Звуки", "Эффекты полёта и интерфейса", state.soundEnabled, onSoundChanged)
            SettingSwitch(
                "Мягкие напоминания",
                "Настройка сохранена; системные уведомления появятся в следующей версии",
                state.gentleReminders,
                onRemindersChanged,
            )
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("Приватность", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            SettingSwitch(
                "Память облачка",
                "Сохранять до 12 заметок check-in только на этом устройстве",
                state.memoryEnabled,
                onMemoryChanged,
            )
            TextButton(onClick = onMemory, modifier = Modifier.fillMaxWidth()) {
                Text("Управление памятью (${state.memories.size})")
            }
            HorizontalDivider(Modifier.padding(vertical = 12.dp))
            Text("О приложении", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Text(
                "CloudBuddy помогает остановиться, назвать эмоцию и выбрать небольшое действие. Он не диагностирует и не заменяет профессиональную помощь.",
                modifier = Modifier.padding(vertical = 12.dp),
            )
            TextButton(onClick = { confirmReset = true }, modifier = Modifier.fillMaxWidth()) {
                Text("Начать заново", color = MaterialTheme.colorScheme.error)
            }
        }
    }
    if (confirmReset) {
        AlertDialog(
            onDismissRequest = { confirmReset = false },
            title = { Text("Удалить локальные данные?") },
            text = { Text("Имя, настроение, прогресс, настройки и воспоминания будут удалены с устройства.") },
            confirmButton = { TextButton(onClick = onReset) { Text("Удалить") } },
            dismissButton = { TextButton(onClick = { confirmReset = false }) { Text("Отмена") } },
        )
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f).padding(end = 12.dp)) {
            Text(title, fontWeight = FontWeight.SemiBold)
            Text(description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}
