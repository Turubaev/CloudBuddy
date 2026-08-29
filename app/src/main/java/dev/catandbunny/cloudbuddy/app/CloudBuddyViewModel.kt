package dev.catandbunny.cloudbuddy.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.AiModel
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.core.model.UserMood
import dev.catandbunny.cloudbuddy.core.model.ChatFunding
import dev.catandbunny.cloudbuddy.core.model.SubscriptionTier
import dev.catandbunny.cloudbuddy.data.CloudBuddyRepository
import dev.catandbunny.cloudbuddy.data.ApiKeyStore
import dev.catandbunny.cloudbuddy.feature.chat.ChatService
import dev.catandbunny.cloudbuddy.feature.subscription.SubscriptionService
import dev.catandbunny.cloudbuddy.feature.subscription.SubscriptionUiState
import android.app.Activity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudBuddyViewModel(
    private val repository: CloudBuddyRepository,
    private val chatService: ChatService,
    private val apiKeyStore: ApiKeyStore,
    private val subscriptionService: SubscriptionService,
) : ViewModel() {

    val buddyState: StateFlow<CloudBuddyState> = repository.state.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CloudBuddyState(),
    )

    private val _messages = MutableStateFlow<List<ChatMessage>>(emptyList())
    val messages: StateFlow<List<ChatMessage>> = _messages.asStateFlow()

    private val _isSending = MutableStateFlow(false)
    val isSending: StateFlow<Boolean> = _isSending.asStateFlow()

    private val _hasPersonalApiKey = MutableStateFlow(apiKeyStore.hasKey())
    val hasPersonalApiKey: StateFlow<Boolean> = _hasPersonalApiKey.asStateFlow()

    private val _personalApiNotice = MutableStateFlow<String?>(null)
    val personalApiNotice: StateFlow<String?> = _personalApiNotice.asStateFlow()

    private val _subscriptionState = MutableStateFlow(
        SubscriptionUiState(isConfigured = subscriptionService.isConfigured),
    )
    val subscriptionState: StateFlow<SubscriptionUiState> = _subscriptionState.asStateFlow()

    init {
        viewModelScope.launch {
            buddyState.first { it.isLoaded }
            refreshSubscription()
        }
    }

    fun completeOnboarding(name: String, personality: Personality) {
        viewModelScope.launch { repository.completeOnboarding(name, personality) }
    }

    fun saveCheckIn(mood: UserMood, note: String) {
        viewModelScope.launch { repository.saveCheckIn(mood, note) }
    }

    fun recordGame(score: Int) {
        viewModelScope.launch { repository.recordGame(score) }
    }

    fun sendMessage(text: String) {
        val message = text.trim().take(1_000)
        if (message.isBlank() || _isSending.value) return
        val userMessage = ChatMessage(System.nanoTime(), message, fromUser = true)
        _messages.value += userMessage
        val history = _messages.value
        _isSending.value = true
        viewModelScope.launch {
            try {
                val reply = chatService.reply(message, buddyState.value, history)
                val responseText = reply.warning?.let { "$it\n\n${reply.text}" } ?: reply.text
                _messages.value += ChatMessage(
                    id = System.nanoTime(),
                    text = responseText,
                    fromUser = false,
                    sourceLabel = reply.sourceLabel,
                )
                repository.recordConversation()
                if (reply.funding == ChatFunding.CLOUD_BUDDY) repository.recordHostedAiMessage()
            } finally {
                _isSending.value = false
            }
        }
    }

    fun ensureGreeting() {
        if (_messages.value.isNotEmpty()) return
        val state = buddyState.value
        _messages.value = listOf(
            ChatMessage(
                id = 0,
                text = "Я ${state.buddyName}. Можешь рассказать, что происходит, или просто немного побыть здесь.",
                fromUser = false,
            ),
        )
    }

    fun setSoundEnabled(enabled: Boolean) = viewModelScope.launch { repository.setSoundEnabled(enabled) }
    fun setGentleReminders(enabled: Boolean) = viewModelScope.launch { repository.setGentleReminders(enabled) }
    fun setMemoryEnabled(enabled: Boolean) = viewModelScope.launch { repository.setMemoryEnabled(enabled) }
    fun setPersonalApiEnabled(enabled: Boolean) = viewModelScope.launch {
        repository.setPersonalApiEnabled(enabled && apiKeyStore.hasKey())
    }
    fun setAiModel(model: AiModel) = viewModelScope.launch { repository.setAiModel(model) }

    fun refreshSubscription() {
        if (_subscriptionState.value.isLoading) return
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, message = null)
        viewModelScope.launch {
            val result = runCatching { subscriptionService.refresh() }.getOrElse {
                dev.catandbunny.cloudbuddy.feature.subscription.SubscriptionResult(
                    tier = buddyState.value.subscriptionTier,
                    message = "Не удалось проверить подписку RuStore. Попробуй ещё раз позже.",
                )
            }
            repository.setSubscriptionTier(result.tier)
            _subscriptionState.value = SubscriptionUiState(
                isConfigured = subscriptionService.isConfigured,
                tier = result.tier,
                priceLabel = result.priceLabel,
                message = result.message,
            )
        }
    }

    fun purchasePlus(activity: Activity) {
        if (_subscriptionState.value.isLoading) return
        _subscriptionState.value = _subscriptionState.value.copy(isLoading = true, message = null)
        viewModelScope.launch {
            val result = runCatching { subscriptionService.purchasePlus(activity) }.getOrElse {
                dev.catandbunny.cloudbuddy.feature.subscription.SubscriptionResult(
                    tier = buddyState.value.subscriptionTier,
                    message = "Платёж не завершён. Деньги не должны быть списаны.",
                )
            }
            repository.setSubscriptionTier(result.tier)
            _subscriptionState.value = SubscriptionUiState(
                isConfigured = subscriptionService.isConfigured,
                tier = result.tier,
                priceLabel = result.priceLabel,
                message = result.message,
            )
        }
    }

    fun savePersonalApiKey(apiKey: String) {
        runCatching { apiKeyStore.save(apiKey) }
            .onSuccess {
                _hasPersonalApiKey.value = true
                _personalApiNotice.value = "Ключ зашифрован и сохранён на устройстве."
                viewModelScope.launch { repository.setPersonalApiEnabled(true) }
            }
            .onFailure {
                _personalApiNotice.value = "Не удалось сохранить ключ в Android Keystore."
            }
    }

    fun deletePersonalApiKey() {
        apiKeyStore.clear()
        _hasPersonalApiKey.value = false
        _personalApiNotice.value = "Личный ключ удалён с устройства."
        viewModelScope.launch { repository.setPersonalApiEnabled(false) }
    }
    fun clearMemories() = viewModelScope.launch { repository.clearMemories() }
    fun reset() {
        apiKeyStore.clear()
        _hasPersonalApiKey.value = false
        _personalApiNotice.value = null
        viewModelScope.launch { repository.reset() }
    }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CloudBuddyViewModel(
                container.repository,
                container.chatService,
                container.apiKeyStore,
                container.subscriptionService,
            ) as T
    }
}
