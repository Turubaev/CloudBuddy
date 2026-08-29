package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.ChatMessage
import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import dev.catandbunny.cloudbuddy.core.model.ChatFunding
import dev.catandbunny.cloudbuddy.data.ApiKeyStore
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject

class DirectOpenAiChatService(
    private val apiKeyStore: ApiKeyStore,
    private val fallback: ChatService,
) : ChatService {
    override suspend fun reply(message: String, state: CloudBuddyState, history: List<ChatMessage>): ChatReply {
        if (CrisisSupport.looksLikeCrisis(message)) return ChatReply(CrisisSupport.message, "Безопасная помощь", ChatFunding.OFFLINE)
        val key = apiKeyStore.read()
            ?: return fallback.reply(message, state, history).copy(
                warning = "Личный API включён, но ключ не найден — отвечаю в offline-режиме.",
            )
        return runCatching { request(key, message, state, history) }.getOrElse { error ->
            fallback.reply(message, state, history).copy(warning = error.userFacingWarning())
        }
    }

    private suspend fun request(
        apiKey: String,
        message: String,
        state: CloudBuddyState,
        history: List<ChatMessage>,
    ): ChatReply = withContext(Dispatchers.IO) {
        val connection = URL(OPENAI_RESPONSES_URL).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 12_000
            connection.readTimeout = 60_000
            connection.doOutput = true
            connection.setRequestProperty("Authorization", "Bearer $apiKey")
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            val input = history.toHistoryJson().let { array ->
                val last = history.lastOrNull()
                if (last?.fromUser != true || last.text != message) {
                    array.put(JSONObject().put("role", "user").put("content", message))
                }
                array
            }
            val payload = JSONObject()
                .put("model", state.aiModel.apiName)
                .put("instructions", companionInstructions(state))
                .put("input", input)
                .put("store", false)
                .put("max_output_tokens", 350)
                .put("reasoning", JSONObject().put("effort", "none"))
                .put("text", JSONObject().put("verbosity", "low"))
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { it.write(payload.toString()) }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val bodyText = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) {
                val detail = runCatching { JSONObject(bodyText).optJSONObject("error")?.optString("message") }.getOrNull()
                throw OpenAiRequestException(connection.responseCode, detail)
            }
            val reply = extractOutputText(JSONObject(bodyText))
            if (reply.isBlank()) throw OpenAiRequestException(502, "OpenAI returned no text")
            ChatReply(reply, "Личный API · ${state.aiModel.title}", ChatFunding.BYOK)
        } finally {
            connection.disconnect()
        }
    }

    private fun extractOutputText(response: JSONObject): String {
        val texts = mutableListOf<String>()
        val output = response.optJSONArray("output") ?: JSONArray()
        for (itemIndex in 0 until output.length()) {
            val content = output.optJSONObject(itemIndex)?.optJSONArray("content") ?: continue
            for (contentIndex in 0 until content.length()) {
                val item = content.optJSONObject(contentIndex) ?: continue
                if (item.optString("type") == "output_text") {
                    item.optString("text").takeIf { it.isNotBlank() }?.let(texts::add)
                }
            }
        }
        return texts.joinToString("\n").trim()
    }

    private fun Throwable.userFacingWarning(): String = when ((this as? OpenAiRequestException)?.statusCode) {
        401, 403 -> "OpenAI отклонил личный ключ. Проверь ключ и права проекта; пока отвечаю в offline-режиме."
        429 -> "Лимит или баланс OpenAI исчерпан; пока отвечаю в offline-режиме."
        else -> "Не удалось связаться с OpenAI; пока отвечаю в offline-режиме."
    }

    private class OpenAiRequestException(val statusCode: Int, detail: String?) :
        RuntimeException(detail ?: "OpenAI request failed with $statusCode")

    private companion object {
        const val OPENAI_RESPONSES_URL = "https://api.openai.com/v1/responses"
    }
}
