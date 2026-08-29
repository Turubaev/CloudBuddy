package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.ChatFunding

data class ChatReply(
    val text: String,
    val sourceLabel: String,
    val funding: ChatFunding,
    val warning: String? = null,
)

interface ChatService {
    suspend fun reply(message: String, state: CloudBuddyState, history: List<ChatMessage>): ChatReply
}

internal fun companionInstructions(state: CloudBuddyState): String = listOf(
    "Ты ${state.buddyName}, вымышленное живое облачко и эмоциональный ИИ-компаньон.",
    when (state.personality) {
        dev.catandbunny.cloudbuddy.core.model.Personality.WARM -> "Говори тепло и бережно."
        dev.catandbunny.cloudbuddy.core.model.Personality.CHEERFUL -> "Добавляй лёгкий добрый юмор, но не шути над болью пользователя."
        dev.catandbunny.cloudbuddy.core.model.Personality.CALM -> "Отвечай спокойно, коротко и конкретно."
        dev.catandbunny.cloudbuddy.core.model.Personality.PLAYFUL -> "Будь немного озорным, но оставайся уважительным."
    },
    "Последняя отмеченная пользователем эмоция: ${state.latestMood.name}.",
    "Отвечай по-русски, естественно, обычно 2–5 предложений.",
    "Не ставь диагнозы, не назначай лечение и не утверждай, что заменяешь психолога.",
    "Не формируй зависимость, не обвиняй за отсутствие и не требуй эксклюзивности.",
    "Сначала признай конкретную эмоцию, затем задай не больше одного вопроса или предложи одно небольшое действие.",
    "При риске самоповреждения прямо рекомендуй немедленно связаться с местной экстренной службой и человеком рядом.",
).joinToString(" ")
