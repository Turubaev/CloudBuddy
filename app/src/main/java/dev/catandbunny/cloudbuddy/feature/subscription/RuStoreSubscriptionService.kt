package dev.catandbunny.cloudbuddy.feature.subscription

import android.app.Activity
import android.content.Intent
import dev.catandbunny.cloudbuddy.core.model.SubscriptionTier
import dev.catandbunny.cloudbuddy.core.model.TariffPolicy
import java.util.Date
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import ru.rustore.sdk.pay.RuStorePayClient
import ru.rustore.sdk.pay.model.PreferredPurchaseType
import ru.rustore.sdk.pay.model.ProductId
import ru.rustore.sdk.pay.model.ProductPurchaseParams
import ru.rustore.sdk.pay.model.ProductPurchaseResult
import ru.rustore.sdk.pay.model.ProductType
import ru.rustore.sdk.pay.model.SdkTheme
import ru.rustore.sdk.pay.model.SubscriptionPurchase
import ru.rustore.sdk.pay.model.SubscriptionPurchaseStatus

class RuStoreSubscriptionService(
    consoleAppId: String,
    private val plusProductId: String,
) : SubscriptionService {
    override val isConfigured: Boolean = consoleAppId.isNotBlank()

    override suspend fun refresh(): SubscriptionResult = withContext(Dispatchers.IO) {
        if (!isConfigured) return@withContext unconfigured()
        val client = RuStorePayClient.instance
        val price = runCatching {
            client.getProductInteractor()
                .getProducts(listOf(ProductId(plusProductId)))
                .await()
                .firstOrNull()
                ?.amountLabel
                ?.value
        }.getOrNull().orEmpty().ifBlank { TariffPolicy.PLUS_PRICE_LABEL }
        val active = client.getPurchaseInteractor()
            .getPurchases(productType = ProductType.SUBSCRIPTION)
            .await()
            .filterIsInstance<SubscriptionPurchase>()
            .any { purchase ->
                purchase.productId.value == plusProductId &&
                    purchase.status == SubscriptionPurchaseStatus.ACTIVE &&
                    purchase.expirationDate.after(Date())
            }
        SubscriptionResult(
            tier = if (active) SubscriptionTier.PLUS else SubscriptionTier.FREE,
            priceLabel = price,
            message = if (active) "Подписка Plus подтверждена RuStore." else null,
        )
    }

    override suspend fun purchasePlus(activity: Activity): SubscriptionResult {
        if (!isConfigured) return unconfigured()
        check(!activity.isFinishing) { "Activity is finishing" }
        suspendCancellableCoroutine<ProductPurchaseResult> { continuation ->
            val task = RuStorePayClient.instance.getPurchaseInteractor().purchase(
            params = ProductPurchaseParams(productId = ProductId(plusProductId)),
            preferredPurchaseType = PreferredPurchaseType.ONE_STEP,
            sdkTheme = SdkTheme.LIGHT,
            purchaseEventListener = null,
            )
            task
                .addOnSuccessListener { result ->
                    if (continuation.isActive) continuation.resume(result)
                }
                .addOnFailureListener { error ->
                    if (continuation.isActive) continuation.resumeWithException(error)
                }
            continuation.invokeOnCancellation { task.cancel() }
        }
        return refresh().let { refreshed ->
            if (refreshed.tier == SubscriptionTier.PLUS) refreshed
            else refreshed.copy(message = "Платёж завершён, статус подписки обновляется. Нажми «Проверить статус» через несколько секунд.")
        }
    }

    override fun handleIntent(intent: Intent?) {
        if (isConfigured) RuStorePayClient.instance.getIntentInteractor().proceedIntent(intent, SdkTheme.LIGHT)
    }

    private fun unconfigured() = SubscriptionResult(
        tier = SubscriptionTier.FREE,
        message = "RuStore ещё не подключён. Free и BYOK уже работают, покупка Plus станет доступна после создания приложения в RuStore Console.",
    )
}
