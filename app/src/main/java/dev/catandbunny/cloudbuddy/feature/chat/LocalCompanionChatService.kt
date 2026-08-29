package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.ChatFunding
import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState

class LocalCompanionChatService(
    private val library: OfflineDialogueLibrary = OfflineDialogueLibrary(),
) : ChatService {
    override suspend fun reply(message: String, state: CloudBuddyState, history: List<ChatMessage>): ChatReply {
        if (CrisisSupport.looksLikeCrisis(message)) {
            return ChatReply(CrisisSupport.message, "Безопасная помощь", ChatFunding.OFFLINE)
        }
        return ChatReply(
            text = library.reply(message, state, history),
            sourceLabel = "Offline · библиотека поддержки",
            funding = ChatFunding.OFFLINE,
        )
    }
}

object CrisisSupport {
    private val crisisMarkers = listOf(
        "хочу умереть", "не хочу жить", "покончить с собой", "самоубий",
        "навредить себе", "убить себя", "suicide", "kill myself", "self harm",
    )

    fun looksLikeCrisis(text: String): Boolean {
        val normalized = text.lowercase()
        return crisisMarkers.any(normalized::contains)
    }

    val message: String = "Мне очень важно, что ты написал об этом. Я не экстренная служба и не могу обеспечить твою безопасность. Если опасность непосредственная — позвони в местную экстренную службу или попроси человека рядом остаться с тобой. Постарайся отойти от всего, чем можно навредить себе, и прямо сейчас свяжись с тем, кому доверяешь."
}
