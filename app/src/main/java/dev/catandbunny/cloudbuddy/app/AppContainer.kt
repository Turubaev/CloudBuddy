package dev.catandbunny.cloudbuddy.app

import android.content.Context
import dev.catandbunny.cloudbuddy.BuildConfig
import dev.catandbunny.cloudbuddy.data.CloudBuddyRepository
import dev.catandbunny.cloudbuddy.data.PreferencesCloudBuddyRepository
import dev.catandbunny.cloudbuddy.feature.chat.BackendChatService
import dev.catandbunny.cloudbuddy.feature.chat.ChatService
import dev.catandbunny.cloudbuddy.feature.chat.LocalCompanionChatService

class AppContainer(context: Context) {
    val repository: CloudBuddyRepository = PreferencesCloudBuddyRepository(context.applicationContext)

    val chatService: ChatService = if (BuildConfig.CLOUDBUDDY_BACKEND_URL.isBlank()) {
        LocalCompanionChatService()
    } else {
        BackendChatService(
            backendUrl = BuildConfig.CLOUDBUDDY_BACKEND_URL,
            fallback = LocalCompanionChatService(),
        )
    }
}
