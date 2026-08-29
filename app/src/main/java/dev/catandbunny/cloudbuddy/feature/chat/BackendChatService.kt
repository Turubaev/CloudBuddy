package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.ChatFunding
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class BackendChatService(
    private val backendUrl: String,
    private val installationId: String,
    private val fallback: ChatService,
) : ChatService {

    override suspend fun reply(message: String, state: CloudBuddyState, history: List<ChatMessage>): ChatReply {
        if (CrisisSupport.looksLikeCrisis(message)) return ChatReply(CrisisSupport.message, "Безопасная помощь", ChatFunding.OFFLINE)
        return runCatching { request(message, state, history) }.getOrElse { error ->
            fallback.reply(message, state, history).copy(
                warning = if ((error as? BackendRequestException)?.code == "quota_exhausted") {
                    "Серверный лимит AI-сообщений исчерпан — отвечаю в offline-режиме."
                } else {
                    "Сервер CloudBuddy недоступен — отвечаю в offline-режиме."
                },
            )
        }
    }

    private suspend fun request(
        message: String,
        state: CloudBuddyState,
        history: List<ChatMessage>,
    ): ChatReply = withContext(Dispatchers.IO) {
        val connection = URL(backendUrl.trimEnd('/') + "/v1/chat").openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 45_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val payload = JSONObject()
                .put("message", message)
                .put("buddyName", state.buddyName)
                .put("personality", state.personality.name)
                .put("mood", state.latestMood.name)
                .put("installationId", installationId)
                .put("requestedTier", state.subscriptionTier.name)
                .put("history", history.toHistoryJson())
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload.toString()) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) {
                val code = runCatching { JSONObject(body).optString("code") }.getOrDefault("")
                throw BackendRequestException(connection.responseCode, code)
            }
            val reply = JSONObject(body).optString("reply").takeIf { it.isNotBlank() }
                ?: error("Invalid backend response")
            ChatReply(reply, "CloudBuddy AI", ChatFunding.CLOUD_BUDDY)
        } finally {
            connection.disconnect()
        }
    }

    private class BackendRequestException(val status: Int, val code: String) : RuntimeException()
}

internal fun List<ChatMessage>.toHistoryJson(): JSONArray = JSONArray().also { array ->
    takeLast(12).forEach { chatMessage ->
        array.put(
            JSONObject()
                .put("role", if (chatMessage.fromUser) "user" else "assistant")
                .put("content", chatMessage.text),
        )
    }
}
