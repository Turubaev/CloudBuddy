package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.core.model.UserMood
import kotlin.math.absoluteValue

enum class OfflineEmotion { ANXIETY, SADNESS, ANGER, FATIGUE, LONELINESS, BOREDOM, POSITIVE, NEUTRAL }
enum class OfflineNeed { BE_HEARD, CALM_DOWN, SMALL_STEP, DISTRACTION, PLAY, GREETING }

data class OfflineIntent(val emotion: OfflineEmotion, val need: OfflineNeed)

object OfflineIntentClassifier {
    private val emotionMarkers = linkedMapOf(
        OfflineEmotion.ANXIETY to listOf("тревог", "тревож", "страш", "паник", "волную", "боюсь", "напряж"),
        OfflineEmotion.SADNESS to listOf("груст", "плохо", "плачу", "тяжело", "расстро"),
        OfflineEmotion.ANGER to listOf("злюсь", "бесит", "раздраж", "ненавиж", "ярост"),
        OfflineEmotion.FATIGUE to listOf("устал", "нет сил", "выгор", "сонн", "измот"),
        OfflineEmotion.LONELINESS to listOf("одинок", "никому", "один ", "одна ", "не с кем"),
        OfflineEmotion.BOREDOM to listOf("скучно", "нечего делать", "тоска"),
        OfflineEmotion.POSITIVE to listOf("рад", "счаст", "получилось", "отлично", "классно"),
    )

    fun classify(message: String, fallbackMood: UserMood): OfflineIntent {
        val text = message.lowercase()
        val need = when {
            listOf("привет", "здравств", "доброе утро", "добрый вечер").any(text::contains) -> OfflineNeed.GREETING
            listOf("игр", "полет", "поигра", "развлеч").any(text::contains) -> OfflineNeed.PLAY
            listOf("успоко", "дых", "паник", "тревог").any(text::contains) -> OfflineNeed.CALM_DOWN
            listOf("отвлеч", "скучно", "нечего делать").any(text::contains) -> OfflineNeed.DISTRACTION
            listOf("что делать", "помоги решить", "с чего начать", "совет").any(text::contains) -> OfflineNeed.SMALL_STEP
            else -> OfflineNeed.BE_HEARD
        }
        val emotion = emotionMarkers.entries.firstOrNull { (_, markers) -> markers.any(text::contains) }?.key
            ?: when (fallbackMood) {
                UserMood.ANXIOUS -> OfflineEmotion.ANXIETY
                UserMood.SAD -> OfflineEmotion.SADNESS
                UserMood.TIRED -> OfflineEmotion.FATIGUE
                UserMood.GREAT, UserMood.GOOD -> OfflineEmotion.POSITIVE
                UserMood.NEUTRAL -> OfflineEmotion.NEUTRAL
            }
        return OfflineIntent(emotion, need)
    }
}

class OfflineDialogueLibrary {
    fun reply(message: String, state: CloudBuddyState, history: List<ChatMessage>): String {
        val intent = OfflineIntentClassifier.classify(message, state.latestMood)
        val turn = history.count { it.fromUser }
        val candidates = when (intent.need) {
            OfflineNeed.GREETING -> greetings(state)
            OfflineNeed.PLAY -> playSuggestions()
            OfflineNeed.CALM_DOWN -> calming(intent.emotion, turn)
            OfflineNeed.DISTRACTION -> distractions(intent.emotion)
            OfflineNeed.SMALL_STEP -> smallSteps(intent.emotion)
            OfflineNeed.BE_HEARD -> emotionalSupport(intent.emotion, state.personality, turn)
        }
        val index = stableIndex("${message.lowercase()}|${state.personality}|$turn", candidates.size)
        return candidates[index]
    }

    private fun greetings(state: CloudBuddyState) = listOf(
        "Привет! Я ${state.buddyName}. Какая сегодня погода у тебя внутри?",
        "Я здесь ☁️ Можно рассказать, как ты, попросить короткую практику или предложить мне полёт.",
        "Привет. Давай начнём без спешки: тебе сейчас больше хочется выговориться или немного переключиться?",
    )

    private fun playSuggestions() = listOf(
        "Летим! Для мягкой передышки выбери «Поток», а для азарта — обычный полёт.",
        "Облачный штаб одобряет перерыв. Спокойный режим поможет выдохнуть, классический — переключить внимание.",
        "Можно устроить короткий полёт на одну минуту. Задача не рекорд, а маленькая смена ритма.",
    )

    private fun calming(emotion: OfflineEmotion, turn: Int): List<String> = when (turn % 3) {
        0 -> listOf(
            "Похоже, напряжение сейчас высокое. Упрись стопами в пол и медленно назови пять вещей, которые видишь, четыре ощущения в теле и три звука вокруг.",
            "Давай сначала вернём телу опору: оглянись, найди один спокойный цвет и сделай выдох чуть длиннее вдоха. Повтори три раза без усилия.",
        )
        1 -> listOf(
            "Хорошо, продолжаем маленькими шагами. Расслабь челюсть и плечи, затем спроси себя: что прямо сейчас действительно происходит, без прогнозов на будущее?",
            "Попробуй положить ладонь на прохладную поверхность и описать ощущение тремя словами. Это не отменит ${emotionLabel(emotion)}, но может вернуть немного пространства.",
        )
        else -> listOf(
            "Проверь, стало ли напряжение хотя бы на один пункт меньше. Если нет — не провал: можно выпить воды, сменить комнату или написать человеку, которому доверяешь.",
            "Теперь выбери одно действие на ближайшие пять минут: вода, окно, короткая прогулка или сообщение близкому. Только одно — этого достаточно.",
        )
    }

    private fun distractions(emotion: OfflineEmotion) = listOf(
        "Давай переключимся на минуту: найди вокруг три круглых предмета, два мягких и один, который приятно держать в руках.",
        "Мини-задание от облачка: придумай нелепое название сегодняшней погоде. Например, «понедельничный туман с шансом на печенье».",
        "Можно открыть спокойный полёт и считать не очки, а пять ровных выдохов. ${emotionLabel(emotion).replaceFirstChar { it.uppercase() }} не обязана исчезнуть сразу.",
    )

    private fun smallSteps(emotion: OfflineEmotion) = listOf(
        "Давай уменьшим задачу. Запиши один результат, который нужен сегодня, и действие длительностью не больше пяти минут. Что может стать таким действием?",
        "Сейчас не нужен идеальный план. Выбери: начать, попросить помощи или сознательно отложить до конкретного времени.",
        "Когда внутри ${emotionLabel(emotion)}, большой шаг кажется тяжелее. Как выглядит самая маленькая версия дела, которую можно закончить прямо сейчас?",
    )

    private fun emotionalSupport(emotion: OfflineEmotion, personality: Personality, turn: Int): List<String> {
        val base = when (emotion) {
            OfflineEmotion.ANXIETY -> listOf(
                "Слышу, что внутри много тревоги. Не нужно решать всё сразу — что пугает сильнее всего именно сейчас?",
                "Тревога часто забегает вперёд. Давай отделим факт, который уже есть, от прогноза, который рисует мысль.",
            )
            OfflineEmotion.SADNESS -> listOf(
                "Мне жаль, что тебе сейчас тяжело. Хочешь назвать, что болит сильнее всего, или лучше немного побыть без разбора причин?",
                "Грусти не обязательно немедленно исчезать. Что могло бы сделать ближайшие десять минут хоть немного мягче?",
            )
            OfflineEmotion.ANGER -> listOf(
                "Похоже, тебя правда задело. Прежде чем действовать, можно дать злости безопасный выход: сильно сжать полотенце, пройтись или записать всё без отправки.",
                "Злость часто показывает нарушенную границу. Какая граница или ожидание здесь оказались задеты?",
            )
            OfflineEmotion.FATIGUE -> listOf(
                "Похоже, ресурса сейчас мало. Что можно сегодня сделать достаточно хорошо, а не идеально?",
                "Усталость — не моральный провал. Две минуты тишины, вода или отмена одной необязательной задачи уже считаются заботой.",
            )
            OfflineEmotion.LONELINESS -> listOf(
                "Одиночество бывает очень тяжёлым. Есть ли человек, которому можно отправить простое «можешь немного побыть со мной на связи»?",
                "Я могу поддержать разговор, но живой контакт тоже важен. Какой самый безопасный и небольшой способ приблизиться к нему сегодня?",
            )
            OfflineEmotion.BOREDOM -> distractions(emotion)
            OfflineEmotion.POSITIVE -> listOf(
                "Здорово это слышать. Давай на секунду заметим: что именно помогло этому случиться?",
                "Сохраним маленький солнечный луч: какую деталь этого момента ты хотел бы запомнить?",
            )
            OfflineEmotion.NEUTRAL -> listOf(
                "Я тебя слышу. Что в этой ситуации сейчас важнее всего?",
                "Можем разобрать одну тучку за раз. С какой начнём?",
            )
        }
        if (turn < 2) return base
        return when (personality) {
            Personality.WARM -> base + "Спасибо, что продолжаешь делиться. Я рядом, и можно двигаться очень медленно."
            Personality.CHEERFUL -> base + "Облачный прогноз допускает маленький шаг без героизма. Какой выберем?"
            Personality.CALM -> base + "Назови один факт, одно чувство и одно доступное действие."
            Personality.PLAYFUL -> base + "Принято в облачный штаб. Какую тучку исследуем первой?"
        }
    }

    private fun emotionLabel(emotion: OfflineEmotion): String = when (emotion) {
        OfflineEmotion.ANXIETY -> "тревожно"
        OfflineEmotion.SADNESS -> "грустно"
        OfflineEmotion.ANGER -> "много злости"
        OfflineEmotion.FATIGUE -> "мало сил"
        OfflineEmotion.LONELINESS -> "одиноко"
        OfflineEmotion.BOREDOM -> "скучно"
        OfflineEmotion.POSITIVE -> "радостно"
        OfflineEmotion.NEUTRAL -> "неопределённо"
    }

    private fun stableIndex(seed: String, size: Int): Int = seed.hashCode().let {
        if (it == Int.MIN_VALUE) 0 else it.absoluteValue % size
    }
}
