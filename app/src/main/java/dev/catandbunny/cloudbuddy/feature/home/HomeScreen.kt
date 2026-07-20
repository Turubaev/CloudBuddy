package dev.catandbunny.cloudbuddy.feature.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
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
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.CloudWeather
import dev.catandbunny.cloudbuddy.ui.component.CloudCharacter

@Composable
fun HomeScreen(
    state: CloudBuddyState,
    onCheckIn: () -> Unit,
    onChat: () -> Unit,
    onClassicGame: () -> Unit,
    onCalmGame: () -> Unit,
    onSettings: () -> Unit,
) {
    val gradient = when (state.weather) {
        CloudWeather.SUNNY -> listOf(Color(0xFFFFF1C7), Color(0xFFD9F3FF))
        CloudWeather.SOFT -> listOf(Color(0xFFEAF7FF), Color(0xFFECE7FF))
        CloudWeather.FOGGY -> listOf(Color(0xFFE6EBF2), Color(0xFFD9E6EE))
        CloudWeather.RAINY -> listOf(Color(0xFFC9DDF0), Color(0xFFD8D4EA))
        CloudWeather.STORMY -> listOf(Color(0xFFAEBBD0), Color(0xFFD0C7DF))
    }
    Column(
        modifier = Modifier.fillMaxSize().background(Brush.verticalGradient(gradient)).verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text("Твоё небо", style = MaterialTheme.typography.labelLarge)
                Text(state.buddyName, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            }
            TextButton(onClick = onSettings) { Text("Настройки") }
        }
        CloudCharacter(
            weather = state.weather,
            modifier = Modifier.fillMaxWidth().height(260.dp),
            secretLevel = state.interactions / 5,
        )
        Text(
            text = homePhrase(state),
            style = MaterialTheme.typography.titleMedium,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
        )
        Button(onClick = onCheckIn, modifier = Modifier.fillMaxWidth().height(56.dp).padding(top = 6.dp)) {
            Text("Какая погода внутри?")
        }
        Row(
            modifier = Modifier.fillMaxWidth().padding(top = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            FilledTonalButton(onClick = onChat, modifier = Modifier.weight(1f)) { Text("Поговорить") }
            FilledTonalButton(onClick = onCalmGame, modifier = Modifier.weight(1f)) { Text("Выдохнуть") }
        }
        Card(
            modifier = Modifier.fillMaxWidth().padding(top = 18.dp),
            shape = RoundedCornerShape(26.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface.copy(alpha = .82f)),
        ) {
            Column(Modifier.padding(18.dp)) {
                Text("Путешествие по небу", fontWeight = FontWeight.Bold)
                Text(
                    journeyText(state.interactions),
                    modifier = Modifier.padding(top = 5.dp, bottom = 12.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    repeat(5) { index ->
                        Box(
                            Modifier.size(30.dp).background(
                                if (index < (state.interactions / 3).coerceAtMost(5)) Color(0xFFFFD166) else MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(15.dp),
                            ),
                            contentAlignment = Alignment.Center,
                        ) { Text(if (index < (state.interactions / 3).coerceAtMost(5)) "★" else "·") }
                    }
                }
            }
        }
        FilledTonalButton(onClick = onClassicGame, modifier = Modifier.fillMaxWidth().padding(top = 12.dp)) {
            Text("Полетать · рекорд ${state.bestScore}")
        }
        if (state.interactions >= 12) {
            Text(
                "Пасхалка: звезда у ${state.buddyName} появилась не случайно ✨",
                modifier = Modifier.padding(18.dp),
                style = MaterialTheme.typography.bodySmall,
                textAlign = TextAlign.Center,
            )
        }
    }
}

private fun homePhrase(state: CloudBuddyState): String = when (state.weather) {
    CloudWeather.SUNNY -> "Сегодня в твоём небе много света. Давай заметим этот момент."
    CloudWeather.SOFT -> "Можно поговорить, полетать или просто побыть рядом."
    CloudWeather.FOGGY -> "Необязательно видеть весь путь. Достаточно следующего маленького шага."
    CloudWeather.RAINY -> "Давай немного замедлимся. Тебе не нужно справляться со всем сразу."
    CloudWeather.STORMY -> "Я рядом. Можно рассказать, что происходит, без правильных слов."
}

private fun journeyText(interactions: Int): String = when {
    interactions < 3 -> "Первая звезда появляется после нескольких спокойных встреч."
    interactions < 8 -> "Вдалеке показались огни тихого воздушного города."
    interactions < 15 -> "Созвездие запоминает моменты, которые важны тебе."
    else -> "Ты открыл секретный слой неба. Здесь облака иногда наблюдают за наблюдателем."
}
