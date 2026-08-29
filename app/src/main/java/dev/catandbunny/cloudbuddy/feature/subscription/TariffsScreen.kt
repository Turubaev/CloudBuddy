package dev.catandbunny.cloudbuddy.feature.subscription

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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.SubscriptionTier
import dev.catandbunny.cloudbuddy.core.model.TariffPolicy
import dev.catandbunny.cloudbuddy.core.model.hostedChatAllowance
import dev.catandbunny.cloudbuddy.ui.component.CloudTopBar

@Composable
fun TariffsScreen(
    state: CloudBuddyState,
    subscription: SubscriptionUiState,
    hasPersonalApiKey: Boolean,
    onBack: () -> Unit,
    onPurchasePlus: () -> Unit,
    onRefresh: () -> Unit,
    onOpenByok: () -> Unit,
) {
    val allowance = state.hostedChatAllowance()
    Column(Modifier.fillMaxSize()) {
        CloudTopBar("Тарифы", onBack)
        Column(
            Modifier.weight(1f).verticalScroll(rememberScrollState()).padding(horizontal = 18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                "Offline-поддержка и игра доступны всем без ограничений.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            TariffCard(
                title = "Free",
                subtitle = if (state.subscriptionTier == SubscriptionTier.FREE && !state.personalApiEnabled) "Текущий тариф" else "Базовый доступ",
                highlighted = state.subscriptionTier == SubscriptionTier.FREE && !state.personalApiEnabled,
                features = listOf(
                    "15 приветственных AI-сообщений",
                    "затем ${TariffPolicy.FREE_DAILY_MESSAGES} AI-сообщений каждый день",
                    "offline-чат, check-in и обе игры",
                ),
                footer = if (state.subscriptionTier == SubscriptionTier.FREE) {
                    if (allowance.isWelcomePack) "Осталось приветственных: ${allowance.remaining}"
                    else "Сегодня осталось: ${allowance.remaining} из ${allowance.limit}"
                } else null,
            )
            TariffCard(
                title = "Plus",
                subtitle = subscription.priceLabel,
                highlighted = state.subscriptionTier == SubscriptionTier.PLUS,
                features = listOf(
                    "${TariffPolicy.PLUS_DAILY_MESSAGES} AI-сообщений каждый день",
                    "расширенная память и персонализация в следующих обновлениях",
                    "подписка управляется через RuStore",
                ),
                footer = if (state.subscriptionTier == SubscriptionTier.PLUS) {
                    "Активен · сегодня осталось ${allowance.remaining}"
                } else null,
            ) {
                if (subscription.isLoading) {
                    CircularProgressIndicator(Modifier.padding(8.dp))
                } else if (state.subscriptionTier == SubscriptionTier.PLUS) {
                    OutlinedButton(onClick = onRefresh, modifier = Modifier.fillMaxWidth()) {
                        Text("Проверить статус")
                    }
                } else {
                    Button(
                        onClick = onPurchasePlus,
                        enabled = subscription.isConfigured,
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text(if (subscription.isConfigured) "Оформить Plus" else "RuStore ещё не подключён") }
                }
            }
            TariffCard(
                title = "BYOK",
                subtitle = "Оплата напрямую OpenAI",
                highlighted = state.personalApiEnabled && hasPersonalApiKey,
                features = listOf(
                    "без лимитов CloudBuddy — действуют лимиты твоего OpenAI-проекта",
                    "ключ зашифрован Android Keystore и хранится на устройстве",
                    "не открывает премиум-возможности Plus",
                ),
                footer = when {
                    state.personalApiEnabled && hasPersonalApiKey -> "Активен · ${state.aiModel.title}"
                    hasPersonalApiKey -> "Ключ сохранён, режим выключен"
                    else -> "Ключ не добавлен"
                },
            ) {
                OutlinedButton(onClick = onOpenByok, modifier = Modifier.fillMaxWidth()) {
                    Text("Настроить личный API")
                }
            }
            subscription.message?.let {
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)) {
                    Text(it, Modifier.padding(14.dp), style = MaterialTheme.typography.bodySmall)
                }
            }
            Text(
                "Подписка продлевается автоматически до отмены в RuStore. Перед публикацией здесь появятся ссылки на условия подписки и политику конфиденциальности.",
                modifier = Modifier.padding(vertical = 8.dp),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TariffCard(
    title: String,
    subtitle: String,
    highlighted: Boolean,
    features: List<String>,
    footer: String? = null,
    action: (@Composable () -> Unit)? = null,
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (highlighted) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(Modifier.padding(17.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                Text(title, modifier = Modifier.weight(1f), style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                if (highlighted) Text("● Активен", color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.labelMedium)
            }
            Text(subtitle, fontWeight = FontWeight.SemiBold)
            features.forEach { Text("• $it", style = MaterialTheme.typography.bodyMedium) }
            footer?.let { Text(it, color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.SemiBold) }
            action?.invoke()
        }
    }
}
