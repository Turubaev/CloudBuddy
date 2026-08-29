package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.hostedChatAllowance

class RoutingChatService(
    private val direct: ChatService,
    private val backend: ChatService?,
    private val local: ChatService,
) : ChatService {
    override suspend fun reply(message: String, state: CloudBuddyState, history: List<ChatMessage>): ChatReply =
        when {
            state.personalApiEnabled -> direct.reply(message, state, history)
            backend != null && state.hostedChatAllowance().canSend -> backend.reply(message, state, history)
            backend != null -> local.reply(message, state, history).copy(
                warning = "Лимит AI-сообщений исчерпан. Offline-чат остаётся доступен без ограничений.",
            )
            else -> local.reply(message, state, history)
        }
}
