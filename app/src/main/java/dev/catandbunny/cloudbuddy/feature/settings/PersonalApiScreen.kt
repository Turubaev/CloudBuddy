package dev.catandbunny.cloudbuddy.feature.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.AiModel
import dev.catandbunny.cloudbuddy.ui.component.CloudTopBar

@Composable
fun PersonalApiScreen(
    enabled: Boolean,
    hasKey: Boolean,
    selectedModel: AiModel,
    notice: String?,
    onBack: () -> Unit,
    onEnabledChanged: (Boolean) -> Unit,
    onModelChanged: (AiModel) -> Unit,
    onSaveKey: (String) -> Unit,
    onDeleteKey: () -> Unit,
) {
    var keyInput by remember { mutableStateOf("") }
    var confirmDelete by remember { mutableStateOf(false) }
    Column(Modifier.fillMaxSize()) {
        CloudTopBar("Личный OpenAI API", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 20.dp),
        ) {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                shape = RoundedCornerShape(22.dp),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text("Экспериментальная опция", fontWeight = FontWeight.Bold)
                    Text(
                        "OpenAI не рекомендует хранить API-ключи в мобильных приложениях. Ключ шифруется Android Keystore, но устройство или приложение всё равно могут быть скомпрометированы. Запросы оплачиваются из твоего OpenAI API-проекта.",
                        modifier = Modifier.padding(top = 6.dp),
                    )
                }
            }
            Row(
                modifier = Modifier.fillMaxWidth().padding(vertical = 18.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(Modifier.weight(1f)) {
                    Text("Использовать личный ключ", fontWeight = FontWeight.SemiBold)
                    Text(
                        if (hasKey) "Ключ сохранён в зашифрованном виде" else "Сначала сохрани ключ",
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
                Switch(checked = enabled && hasKey, onCheckedChange = onEnabledChanged, enabled = hasKey)
            }
            notice?.let {
                Text(
                    it,
                    modifier = Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    color = MaterialTheme.colorScheme.primary,
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            OutlinedTextField(
                value = keyInput,
                onValueChange = { keyInput = it.trim().take(300) },
                modifier = Modifier.fillMaxWidth(),
                label = { Text(if (hasKey) "Новый ключ (заменит текущий)" else "OpenAI API key") },
                visualTransformation = PasswordVisualTransformation(),
                keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
                singleLine = true,
            )
            Button(
                onClick = {
                    onSaveKey(keyInput)
                    keyInput = ""
                },
                modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
                enabled = keyInput.isNotBlank(),
            ) { Text(if (hasKey) "Заменить ключ" else "Сохранить и включить") }
            if (hasKey) {
                TextButton(onClick = { confirmDelete = true }, modifier = Modifier.fillMaxWidth()) {
                    Text("Удалить ключ", color = MaterialTheme.colorScheme.error)
                }
            }
            Text("Модель", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, modifier = Modifier.padding(top = 18.dp, bottom = 6.dp))
            AiModel.entries.forEach { model ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { onModelChanged(model) },
                    shape = RoundedCornerShape(20.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (selectedModel == model) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                    ),
                ) {
                    Column(Modifier.padding(15.dp)) {
                        Text(model.title, fontWeight = FontWeight.Bold)
                        Text(model.description, style = MaterialTheme.typography.bodySmall)
                        Text(model.apiName, style = MaterialTheme.typography.labelSmall, modifier = Modifier.padding(top = 3.dp))
                    }
                }
            }
            Text(
                "CloudBuddy отправляет OpenAI до 12 последних сообщений текущей сессии. История чата на устройстве не сохраняется.",
                modifier = Modifier.padding(vertical = 18.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
    if (confirmDelete) {
        AlertDialog(
            onDismissRequest = { confirmDelete = false },
            title = { Text("Удалить личный ключ?") },
            text = { Text("Зашифрованное значение будет удалено с устройства, а чат вернётся к backend/offline-режиму.") },
            confirmButton = {
                TextButton(onClick = { onDeleteKey(); confirmDelete = false }) { Text("Удалить") }
            },
            dismissButton = { TextButton(onClick = { confirmDelete = false }) { Text("Отмена") } },
        )
    }
}
