package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState

interface ChatService {
    suspend fun reply(message: String, state: CloudBuddyState): String
}
