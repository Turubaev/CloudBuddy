package dev.catandbunny.cloudbuddy.feature.chat

import dev.catandbunny.cloudbuddy.core.model.CloudBuddyState
import java.net.HttpURLConnection
import java.net.URL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class BackendChatService(
    private val backendUrl: String,
    private val fallback: ChatService,
) : ChatService {

    override suspend fun reply(message: String, state: CloudBuddyState): String {
        if (CrisisSupport.looksLikeCrisis(message)) return CrisisSupport.message
        return runCatching { request(message, state) }
            .getOrElse { fallback.reply(message, state) }
    }

    private suspend fun request(message: String, state: CloudBuddyState): String = withContext(Dispatchers.IO) {
        val endpoint = backendUrl.trimEnd('/') + "/v1/chat"
        val connection = URL(endpoint).openConnection() as HttpURLConnection
        try {
            connection.requestMethod = "POST"
            connection.connectTimeout = 10_000
            connection.readTimeout = 30_000
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json; charset=utf-8")
            connection.outputStream.bufferedWriter(Charsets.UTF_8).use { writer ->
                writer.write(
                    """{"message":"${message.jsonEscape()}","buddyName":"${state.buddyName.jsonEscape()}","personality":"${state.personality.name}","mood":"${state.latestMood.name}"}""",
                )
            }
            val stream = if (connection.responseCode in 200..299) connection.inputStream else connection.errorStream
            val body = stream?.bufferedReader(Charsets.UTF_8)?.use { it.readText() }.orEmpty()
            if (connection.responseCode !in 200..299) error("Backend returned ${connection.responseCode}")
            Regex("\\\"reply\\\"\\s*:\\s*\\\"((?:\\\\.|[^\\\"])*)\\\"")
                .find(body)
                ?.groupValues
                ?.get(1)
                ?.jsonUnescape()
                ?.takeIf { it.isNotBlank() }
                ?: error("Invalid backend response")
        } finally {
            connection.disconnect()
        }
    }

    private fun String.jsonEscape(): String = buildString {
        for (character in this@jsonEscape) {
            append(
                when (character) {
                    '\\' -> "\\\\"
                    '"' -> "\\\""
                    '\n' -> "\\n"
                    '\r' -> "\\r"
                    '\t' -> "\\t"
                    else -> character
                },
            )
        }
    }

    private fun String.jsonUnescape(): String = replace("\\n", "\n")
        .replace("\\t", "\t")
        .replace("\\\"", "\"")
        .replace("\\\\", "\\")
}
