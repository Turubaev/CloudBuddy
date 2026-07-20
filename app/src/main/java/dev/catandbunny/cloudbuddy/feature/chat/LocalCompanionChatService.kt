package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.core.model.UserMood
import kotlin.math.absoluteValue

class LocalCompanionChatService : ChatService {
    override suspend fun reply(message: String, state: CloudBuddyState): String {
        if (CrisisSupport.looksLikeCrisis(message)) return CrisisSupport.message

        val normalized = message.lowercase()
        if (normalized.contains("привет") || normalized.contains("здравств")) {
            return "Привет! Я ${state.buddyName}. Какая сегодня погода у тебя внутри?"
        }
        if (normalized.contains("тревог") || normalized.contains("страш")) {
            return "Я рядом. Давай не решать всё сразу: почувствуй опору под ногами и назови три вещи, которые видишь вокруг. Потом можно сделать один медленный вдох."
        }
        if (normalized.contains("устал") || normalized.contains("нет сил")) {
            return "Похоже, сил сейчас немного. Можно ничего не доказывать: две минуты тихого режима или короткий полёт — уже достаточно."
        }
        if (normalized.contains("груст") || normalized.contains("плохо")) {
            return "Мне жаль, что тебе сейчас тяжело. Хочешь рассказать, что давит сильнее всего, или лучше немного отвлечься?"
        }
        if (normalized.contains("игр") || normalized.contains("полет")) {
            return "Летим! Для выдоха выбери «Поток», а если хочется азарта — обычный полёт."
        }

        val variants = when (state.personality) {
            Personality.WARM -> listOf(
                "Я тебя слышу. Что в этой ситуации сейчас самое трудное?",
                "Спасибо, что поделился. Хочешь, я просто побуду рядом или попробуем найти маленький следующий шаг?",
            )
            Personality.CHEERFUL -> listOf(
                "Понял. Предлагаю план: один маленький шаг, один глубокий вдох и никакого героизма до обеда.",
                "Звучит как погода с сюрпризами. Разберём одну тучку за раз?",
            )
            Personality.CALM -> listOf(
                "Я рядом. Что из происходящего ты можешь изменить сегодня?",
                "Давай замедлимся. Назови одну мысль, одно чувство и одно действие, которое сейчас доступно.",
            )
            Personality.PLAYFUL -> listOf(
                "Принято в облачный штаб. Какую тучку расследуем первой?",
                "Кажется, внутренний синоптик слегка драматизирует. Проверим факты вместе?",
            )
        }
        return variants[message.hashCode().absoluteValue % variants.size]
    }
}

object CrisisSupport {
    private val crisisMarkers = listOf(
        "хочу умереть", "не хочу жить", "покончить с собой", "самоубий",
        "навредить себе", "убить себя", "suicide", "kill myself", "self harm",
    )

    fun looksLikeCrisis(text: String): Boolean {
        val normalized = text.lowercase()
        return crisisMarkers.any(normalized::contains)
    }

    val message: String = "Мне очень важно, что ты написал об этом. Я не экстренная служба и не могу обеспечить твою безопасность. Если опасность непосредственная — позвони в местную экстренную службу или попроси человека рядом остаться с тобой. Постарайся отойти от всего, чем можно навредить себе, и прямо сейчас свяжись с тем, кому доверяешь."
}
