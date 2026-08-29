package dev.catandbunny.cloudbuddy.core.model

import java.time.LocalDate

enum class Personality(val title: String, val description: String) {
    WARM("Тёплый", "Бережно поддерживает и помогает выдохнуть"),
    CHEERFUL("Весёлый", "Шутит и мягко переключает внимание"),
    CALM("Спокойный", "Говорит коротко и предлагает практики"),
    PLAYFUL("Озорной", "Любит сюрпризы и ломает четвёртую стену"),
}

enum class UserMood(val emoji: String, val title: String) {
    GREAT("☀️", "Отлично"),
    GOOD("🌤️", "Неплохо"),
    NEUTRAL("☁️", "Спокойно"),
    TIRED("🌫️", "Усталость"),
    ANXIOUS("🌧️", "Тревожно"),
    SAD("⛈️", "Грустно"),
}

enum class CloudWeather {
    SUNNY, SOFT, FOGGY, RAINY, STORMY,
}

enum class AiModel(val apiName: String, val title: String, val description: String) {
    LUNA("gpt-5.6-luna", "Luna", "Экономичный вариант для частых разговоров"),
    TERRA("gpt-5.6-terra", "Terra", "Баланс качества, скорости и стоимости"),
    SOL("gpt-5.6-sol", "Sol", "Максимальное качество, выше стоимость"),
}

enum class SubscriptionTier(val title: String) {
    FREE("Free"),
    PLUS("Plus"),
}

enum class ChatFunding(val title: String) {
    CLOUD_BUDDY("CloudBuddy"),
    BYOK("Личный API"),
    OFFLINE("Offline"),
}

object TariffPolicy {
    const val FREE_WELCOME_MESSAGES = 15
    const val FREE_DAILY_MESSAGES = 5
    const val PLUS_DAILY_MESSAGES = 30
    const val PLUS_PRICE_LABEL = "299 ₽ / месяц"
    const val PLUS_PRODUCT_ID = "cloudbuddy_plus_monthly"
}

data class HostedChatAllowance(
    val remaining: Int,
    val limit: Int,
    val isWelcomePack: Boolean,
) {
    val canSend: Boolean get() = remaining > 0
}

enum class GameMode(val title: String, val description: String) {
    CLASSIC("Полёт", "Пролетай между потоками ветра и ставь рекорд"),
    CALM("Поток", "Мягкий режим без проигрыша и спешки"),
}

data class CheckIn(
    val mood: UserMood,
    val note: String,
    val createdAt: Long,
)

data class ChatMessage(
    val id: Long,
    val text: String,
    val fromUser: Boolean,
    val sourceLabel: String? = null,
)

data class CloudBuddyState(
    val isLoaded: Boolean = false,
    val onboardingComplete: Boolean = false,
    val buddyName: String = "Луми",
    val personality: Personality = Personality.WARM,
    val latestMood: UserMood = UserMood.NEUTRAL,
    val latestNote: String = "",
    val weather: CloudWeather = CloudWeather.SOFT,
    val interactions: Int = 0,
    val bestScore: Int = 0,
    val soundEnabled: Boolean = true,
    val gentleReminders: Boolean = false,
    val memoryEnabled: Boolean = true,
    val memories: List<String> = emptyList(),
    val personalApiEnabled: Boolean = false,
    val aiModel: AiModel = AiModel.LUNA,
    val subscriptionTier: SubscriptionTier = SubscriptionTier.FREE,
    val welcomeAiMessagesUsed: Int = 0,
    val dailyAiMessagesUsed: Int = 0,
    val dailyAiEpochDay: Long = LocalDate.now().toEpochDay(),
)

fun CloudBuddyState.hostedChatAllowance(todayEpochDay: Long = LocalDate.now().toEpochDay()): HostedChatAllowance {
    if (subscriptionTier == SubscriptionTier.PLUS) {
        val usedToday = if (dailyAiEpochDay == todayEpochDay) dailyAiMessagesUsed else 0
        return HostedChatAllowance(
            remaining = (TariffPolicy.PLUS_DAILY_MESSAGES - usedToday).coerceAtLeast(0),
            limit = TariffPolicy.PLUS_DAILY_MESSAGES,
            isWelcomePack = false,
        )
    }
    val welcomeRemaining = (TariffPolicy.FREE_WELCOME_MESSAGES - welcomeAiMessagesUsed).coerceAtLeast(0)
    if (welcomeRemaining > 0) {
        return HostedChatAllowance(
            remaining = welcomeRemaining,
            limit = TariffPolicy.FREE_WELCOME_MESSAGES,
            isWelcomePack = true,
        )
    }
    val usedToday = if (dailyAiEpochDay == todayEpochDay) dailyAiMessagesUsed else 0
    return HostedChatAllowance(
        remaining = (TariffPolicy.FREE_DAILY_MESSAGES - usedToday).coerceAtLeast(0),
        limit = TariffPolicy.FREE_DAILY_MESSAGES,
        isWelcomePack = false,
    )
}

fun weatherFor(mood: UserMood): CloudWeather = when (mood) {
    UserMood.GREAT -> CloudWeather.SUNNY
    UserMood.GOOD, UserMood.NEUTRAL -> CloudWeather.SOFT
    UserMood.TIRED -> CloudWeather.FOGGY
    UserMood.ANXIOUS -> CloudWeather.RAINY
    UserMood.SAD -> CloudWeather.STORMY
}
