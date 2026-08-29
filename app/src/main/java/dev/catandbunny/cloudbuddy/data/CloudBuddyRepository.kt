package dev.catandbunny.cloudbuddy.data

import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.AiModel
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.core.model.UserMood
import dev.catandbunny.cloudbuddy.core.model.SubscriptionTier
import kotlinx.coroutines.flow.Flow

interface CloudBuddyRepository {
    val state: Flow<CloudBuddyState>

    suspend fun completeOnboarding(name: String, personality: Personality)
    suspend fun saveCheckIn(mood: UserMood, note: String)
    suspend fun recordGame(score: Int)
    suspend fun recordConversation()
    suspend fun recordHostedAiMessage()
    suspend fun setSubscriptionTier(tier: SubscriptionTier)
    suspend fun setSoundEnabled(enabled: Boolean)
    suspend fun setGentleReminders(enabled: Boolean)
    suspend fun setMemoryEnabled(enabled: Boolean)
    suspend fun setPersonalApiEnabled(enabled: Boolean)
    suspend fun setAiModel(model: AiModel)
    suspend fun clearMemories()
    suspend fun reset()
}
