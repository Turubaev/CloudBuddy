package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.UserMood
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class OfflineDialogueLibraryTest {
    @Test
    fun classifiesAnxietyAndCalmingNeed() {
        val intent = OfflineIntentClassifier.classify("Мне тревожно, помоги успокоиться", UserMood.NEUTRAL)

        assertEquals(OfflineEmotion.ANXIETY, intent.emotion)
        assertEquals(OfflineNeed.CALM_DOWN, intent.need)
    }

    @Test
    fun usesCheckInMoodAsFallback() {
        val intent = OfflineIntentClassifier.classify("Не знаю, что сказать", UserMood.SAD)

        assertEquals(OfflineEmotion.SADNESS, intent.emotion)
    }

    @Test
    fun dialogueChangesStageWithHistory() {
        val library = OfflineDialogueLibrary()
        val state = CloudBuddyState(latestMood = UserMood.ANXIOUS)
        val first = library.reply("Мне тревожно", state, emptyList())
        val continued = library.reply(
            "Мне тревожно",
            state,
            listOf(ChatMessage(1, "Мне тревожно", true), ChatMessage(2, first, false)),
        )

        assertNotEquals(first, continued)
    }

    @Test
    fun crisisMarkerIsRecognizedOffline() {
        assertTrue(CrisisSupport.looksLikeCrisis("Я не хочу жить"))
    }
}
