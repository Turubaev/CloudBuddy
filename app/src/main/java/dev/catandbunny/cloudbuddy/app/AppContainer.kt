package dev.catandbunny.cloudbuddy.app

import android.content.Context
import dev.catandbunny.cloudbuddy.BuildConfig
import dev.catandbunny.cloudbuddy.data.CloudBuddyRepository
import dev.catandbunny.cloudbuddy.data.ApiKeyStore
import dev.catandbunny.cloudbuddy.data.PreferencesCloudBuddyRepository
import dev.catandbunny.cloudbuddy.data.InstallationIdStore
import dev.catandbunny.cloudbuddy.feature.chat.BackendChatService
import dev.catandbunny.cloudbuddy.feature.chat.ChatService
import dev.catandbunny.cloudbuddy.feature.chat.LocalCompanionChatService
import dev.catandbunny.cloudbuddy.feature.chat.DirectOpenAiChatService
import dev.catandbunny.cloudbuddy.feature.chat.RoutingChatService
import dev.catandbunny.cloudbuddy.feature.subscription.SubscriptionService
import dev.catandbunny.cloudbuddy.feature.subscription.UnconfiguredSubscriptionService
import dev.catandbunny.cloudbuddy.feature.subscription.RuStoreSubscriptionService

class AppContainer(context: Context) {
    val repository: CloudBuddyRepository = PreferencesCloudBuddyRepository(context.applicationContext)
    val apiKeyStore = ApiKeyStore(context.applicationContext)
    private val installationIdStore = InstallationIdStore(context.applicationContext)
    val subscriptionService: SubscriptionService = if (BuildConfig.RUSTORE_CONSOLE_APP_ID.isBlank()) {
        UnconfiguredSubscriptionService()
    } else {
        RuStoreSubscriptionService(
            consoleAppId = BuildConfig.RUSTORE_CONSOLE_APP_ID,
            plusProductId = BuildConfig.RUSTORE_PLUS_PRODUCT_ID,
        )
    }

    val chatService: ChatService = LocalCompanionChatService().let { local ->
        val backend = BuildConfig.CLOUDBUDDY_BACKEND_URL.takeIf { it.isNotBlank() }?.let { url ->
            BackendChatService(
                backendUrl = url,
                installationId = installationIdStore.get(),
                fallback = local,
            )
        }
        val direct = DirectOpenAiChatService(apiKeyStore = apiKeyStore, fallback = backend ?: local)
        RoutingChatService(direct = direct, backend = backend, local = local)
    }
}
