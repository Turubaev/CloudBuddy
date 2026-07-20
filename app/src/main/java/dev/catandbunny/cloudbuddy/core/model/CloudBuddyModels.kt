package dev.catandbunny.cloudbuddy.core.model

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
)

fun weatherFor(mood: UserMood): CloudWeather = when (mood) {
    UserMood.GREAT -> CloudWeather.SUNNY
    UserMood.GOOD, UserMood.NEUTRAL -> CloudWeather.SOFT
    UserMood.TIRED -> CloudWeather.FOGGY
    UserMood.ANXIOUS -> CloudWeather.RAINY
    UserMood.SAD -> CloudWeather.STORMY
}
