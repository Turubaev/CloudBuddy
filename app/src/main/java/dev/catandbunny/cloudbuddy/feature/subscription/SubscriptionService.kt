package dev.catandbunny.cloudbuddy.feature.subscription

import android.app.Activity
import android.content.Intent
import dev.catandbunny.cloudbuddy.core.model.SubscriptionTier
import dev.catandbunny.cloudbuddy.core.model.TariffPolicy

data class SubscriptionUiState(
    val isLoading: Boolean = false,
    val isConfigured: Boolean = false,
    val tier: SubscriptionTier = SubscriptionTier.FREE,
    val priceLabel: String = TariffPolicy.PLUS_PRICE_LABEL,
    val message: String? = null,
)

data class SubscriptionResult(
    val tier: SubscriptionTier,
    val priceLabel: String = TariffPolicy.PLUS_PRICE_LABEL,
    val message: String? = null,
)

interface SubscriptionService {
    val isConfigured: Boolean
    suspend fun refresh(): SubscriptionResult
    suspend fun purchasePlus(activity: Activity): SubscriptionResult
    fun handleIntent(intent: Intent?)
}

class UnconfiguredSubscriptionService : SubscriptionService {
    override val isConfigured: Boolean = false

    override suspend fun refresh() = SubscriptionResult(
        tier = SubscriptionTier.FREE,
        message = "RuStore ещё не подключён. Free и BYOK уже работают, покупка Plus станет доступна после создания приложения в RuStore Console.",
    )

    override suspend fun purchasePlus(activity: Activity) = refresh()
    override fun handleIntent(intent: Intent?) = Unit
}
