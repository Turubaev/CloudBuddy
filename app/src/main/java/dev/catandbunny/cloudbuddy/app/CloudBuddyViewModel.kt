package dev.catandbunny.cloudbuddy.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.Personality
import dev.catandbunny.cloudbuddy.core.model.UserMood
import dev.catandbunny.cloudbuddy.data.CloudBuddyRepository
import dev.catandbunny.cloudbuddy.feature.chat.ChatService
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class CloudBuddyViewModel(
    private val repository: CloudBuddyRepository,
    private val chatService: ChatService,
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
        _isSending.value = true
        viewModelScope.launch {
            val reply = chatService.reply(message, buddyState.value)
            _messages.value += ChatMessage(System.nanoTime(), reply, fromUser = false)
            repository.recordConversation()
            _isSending.value = false
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
    fun clearMemories() = viewModelScope.launch { repository.clearMemories() }
    fun reset() = viewModelScope.launch { repository.reset() }

    class Factory(private val container: AppContainer) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            CloudBuddyViewModel(container.repository, container.chatService) as T
    }
}
