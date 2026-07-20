package dev.catandbunny.cloudbuddy.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.core.model.UserMood
import dev.catandbunny.cloudbuddy.core.model.weatherFor
import java.net.URLDecoder
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.cloudBuddyDataStore by preferencesDataStore(name = "cloud_buddy")

class PreferencesCloudBuddyRepository(
    private val context: Context,
) : CloudBuddyRepository {

    private object Keys {
        val onboarding = booleanPreferencesKey("onboarding_complete")
        val name = stringPreferencesKey("buddy_name")
        val personality = stringPreferencesKey("personality")
        val mood = stringPreferencesKey("latest_mood")
        val note = stringPreferencesKey("latest_note")
        val interactions = intPreferencesKey("interactions")
        val bestScore = intPreferencesKey("best_score")
        val sound = booleanPreferencesKey("sound")
        val reminders = booleanPreferencesKey("gentle_reminders")
        val memory = booleanPreferencesKey("memory_enabled")
        val memories = stringPreferencesKey("memories")
    }

    override val state: Flow<CloudBuddyState> = context.cloudBuddyDataStore.data.map { preferences ->
        val mood = preferences[Keys.mood].toEnumOrDefault(UserMood.NEUTRAL)
        CloudBuddyState(
            isLoaded = true,
            onboardingComplete = preferences[Keys.onboarding] ?: false,
            buddyName = preferences[Keys.name] ?: "Луми",
            personality = preferences[Keys.personality].toEnumOrDefault(Personality.WARM),
            latestMood = mood,
            latestNote = preferences[Keys.note].orEmpty(),
            weather = weatherFor(mood),
            interactions = preferences[Keys.interactions] ?: 0,
            bestScore = preferences[Keys.bestScore] ?: 0,
            soundEnabled = preferences[Keys.sound] ?: true,
            gentleReminders = preferences[Keys.reminders] ?: false,
            memoryEnabled = preferences[Keys.memory] ?: true,
            memories = decodeMemories(preferences[Keys.memories].orEmpty()),
        )
    }

    override suspend fun completeOnboarding(name: String, personality: Personality) {
        context.cloudBuddyDataStore.edit {
            it[Keys.onboarding] = true
            it[Keys.name] = name.trim().ifBlank { "Луми" }.take(20)
            it[Keys.personality] = personality.name
            it[Keys.interactions] = (it[Keys.interactions] ?: 0) + 1
        }
    }

    override suspend fun saveCheckIn(mood: UserMood, note: String) {
        context.cloudBuddyDataStore.edit {
            it[Keys.mood] = mood.name
            it[Keys.note] = note.trim().take(240)
            it[Keys.interactions] = (it[Keys.interactions] ?: 0) + 1
            if ((it[Keys.memory] ?: true) && note.isNotBlank()) {
                val current = decodeMemories(it[Keys.memories].orEmpty())
                it[Keys.memories] = encodeMemories((listOf(note.trim()) + current).distinct().take(12))
            }
        }
    }

    override suspend fun recordGame(score: Int) {
        context.cloudBuddyDataStore.edit {
            it[Keys.bestScore] = maxOf(it[Keys.bestScore] ?: 0, score)
            it[Keys.interactions] = (it[Keys.interactions] ?: 0) + 1
        }
    }

    override suspend fun recordConversation() {
        context.cloudBuddyDataStore.edit {
            it[Keys.interactions] = (it[Keys.interactions] ?: 0) + 1
        }
    }

    override suspend fun setSoundEnabled(enabled: Boolean) = update(Keys.sound, enabled)
    override suspend fun setGentleReminders(enabled: Boolean) = update(Keys.reminders, enabled)
    override suspend fun setMemoryEnabled(enabled: Boolean) = update(Keys.memory, enabled)

    override suspend fun clearMemories() {
        context.cloudBuddyDataStore.edit {
            it.remove(Keys.memories)
            it.remove(Keys.note)
        }
    }

    override suspend fun reset() {
        context.cloudBuddyDataStore.edit { it.clear() }
    }

    private suspend fun update(key: androidx.datastore.preferences.core.Preferences.Key<Boolean>, value: Boolean) {
        context.cloudBuddyDataStore.edit { it[key] = value }
    }

    private inline fun <reified T : Enum<T>> String?.toEnumOrDefault(default: T): T =
        this?.let { runCatching { enumValueOf<T>(it) }.getOrNull() } ?: default

    private fun encodeMemories(items: List<String>): String = items.joinToString("|") {
        URLEncoder.encode(it, StandardCharsets.UTF_8.name())
    }

    private fun decodeMemories(value: String): List<String> = value
        .split('|')
        .filter { it.isNotBlank() }
        .mapNotNull { encoded ->
            runCatching { URLDecoder.decode(encoded, StandardCharsets.UTF_8.name()) }.getOrNull()
        }
}
