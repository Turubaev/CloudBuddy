package dev.catandbunny.cloudbuddy.feature.onboarding

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import dev.catandbunny.cloudbuddy.core.model.CloudWeather
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.ui.component.CloudCharacter

@Composable
fun OnboardingScreen(onContinue: (String, Personality) -> Unit) {
    var name by remember { mutableStateOf("Луми") }
    var selected by remember { mutableStateOf(Personality.WARM) }
    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
        verticalArrangement = Arrangement.Center,
    ) {
        CloudCharacter(CloudWeather.SUNNY, Modifier.fillMaxWidth().height(220.dp))
        Text("Привет. Я буду рядом.", style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
        Text(
            "CloudBuddy — эмоциональный компаньон: здесь можно выдохнуть, поговорить или немного полетать. Я не заменяю врача или психолога.",
            modifier = Modifier.padding(top = 10.dp, bottom = 22.dp),
            style = MaterialTheme.typography.bodyLarge,
        )
        OutlinedTextField(
            value = name,
            onValueChange = { name = it.take(20) },
            modifier = Modifier.fillMaxWidth(),
            label = { Text("Как зовут облачко?") },
            singleLine = true,
        )
        Text("Выбери характер", modifier = Modifier.padding(top = 24.dp, bottom = 10.dp), fontWeight = FontWeight.SemiBold)
        Personality.entries.forEach { personality ->
            Card(
                modifier = Modifier.fillMaxWidth().padding(vertical = 5.dp).clickable { selected = personality },
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selected == personality) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                ),
            ) {
                Column(Modifier.padding(16.dp)) {
                    Text(personality.title, fontWeight = FontWeight.Bold)
                    Text(personality.description, style = MaterialTheme.typography.bodyMedium)
                }
            }
        }
        Spacer(Modifier.size(18.dp))
        Button(
            onClick = { onContinue(name, selected) },
            modifier = Modifier.fillMaxWidth().height(54.dp),
            enabled = name.isNotBlank(),
        ) { Text("Познакомиться") }
        Text(
            "Память и любые записи можно отключить или удалить в настройках.",
            modifier = Modifier.padding(top = 12.dp),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
